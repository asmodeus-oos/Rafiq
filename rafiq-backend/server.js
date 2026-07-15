const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');

const app = express();
app.use(cors());

const server = http.createServer(app);
const io = new Server(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

// In-memory data store for the MVP signaling
const users = new Map(); // socketId -> userId
const waitingQueue = []; // Users waiting for random call

io.on('connection', (socket) => {
    console.log(`User connected: ${socket.id}`);

    // Auth & Setup
    socket.on('register', (userId) => {
        users.set(socket.id, userId);
        socket.join(userId); // Join room for direct messages
        console.log(`User registered: ${userId}`);
    });

    // Chat Module
    socket.on('send_message', (data) => {
        const { receiverId, message } = data;
        io.to(receiverId).emit('receive_message', {
            senderId: users.get(socket.id),
            message,
            timestamp: Date.now()
        });
    });

    socket.on('message_read', (data) => {
        const { senderId, messageId } = data;
        io.to(senderId).emit('message_status_update', {
            messageId,
            status: 'READ'
        });
    });

    // Random Call Matching
    socket.on('join_random_queue', (data) => {
        const userId = users.get(socket.id);
        const { gender, preferences } = data;
        
        console.log(`User ${userId} joined random queue`);
        
        // Simple matching logic
        if (waitingQueue.length > 0) {
            const match = waitingQueue.shift();
            const roomId = `room_${Date.now()}`;
            
            // Notify both users of the match
            io.to(socket.id).emit('match_found', { matchId: match.userId, roomId });
            io.to(match.socketId).emit('match_found', { matchId: userId, roomId });
        } else {
            waitingQueue.push({ socketId: socket.id, userId, gender, preferences });
        }
    });

    socket.on('leave_queue', () => {
        const index = waitingQueue.findIndex(u => u.socketId === socket.id);
        if (index !== -1) waitingQueue.splice(index, 1);
    });

    // WebRTC Signaling
    socket.on('offer', (data) => {
        const { targetId, offer, roomId } = data;
        io.to(targetId).emit('offer', { senderId: users.get(socket.id), offer, roomId });
    });

    socket.on('answer', (data) => {
        const { targetId, answer, roomId } = data;
        io.to(targetId).emit('answer', { senderId: users.get(socket.id), answer, roomId });
    });

    socket.on('ice_candidate', (data) => {
        const { targetId, candidate, roomId } = data;
        io.to(targetId).emit('ice_candidate', { senderId: users.get(socket.id), candidate, roomId });
    });

    socket.on('end_call', (data) => {
        const { targetId, roomId } = data;
        io.to(targetId).emit('call_ended', { senderId: users.get(socket.id), roomId });
    });

    socket.on('disconnect', () => {
        console.log(`User disconnected: ${socket.id}`);
        users.delete(socket.id);
        const index = waitingQueue.findIndex(u => u.socketId === socket.id);
        if (index !== -1) waitingQueue.splice(index, 1);
    });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`RAFIQ Signaling Server running on port ${PORT}`);
});
