# Design System Inspired by ME Gallery

> Auto-extracted from `https://portofolix-three.vercel.app/` on 2026-07-12

## 1. Visual Theme & Atmosphere

Friendly, approachable design with rounded shapes and generous whitespace.

The hero section leads with "Mohamed Elsayed Abdullatif Khalid Hegazy" followed by "Dental Student | UI/UX Designer | Front-End Developer | Technology Enthusiast".

**Key Characteristics:**
- Outfit as the heading font
- Plus Jakarta Sans as the body font for all running text
- Heading weight 800, letter-spacing -1.56px
- Light/white background (#f5f7fa) as the primary canvas
- Primary accent `#d4af37` used for CTAs and brand highlights
- 8 shadow level(s) detected — tinted shadows
- Rounded corners (50px+) creating a friendly, approachable feel
- Tags: light, rounded, colorful, sans-serif

## 2. Color Palette & Roles

### Primary
- **Primary Accent** (`#d4af37`) · `--color-primary`: Brand color, CTA backgrounds, link text, interactive highlights.
- **Secondary Accent** (`#a87c11`) · `--color-secondary`: Secondary brand, hover states, complementary highlights.
- **Background** (`#f5f7fa`) · `--color-bg`: Page background, primary canvas.
- **Background Secondary** (`#000000`) · `--color-bg-secondary`: Cards, surfaces, alternating sections.

### Text
- **Text Primary** (`#475569`) · `--color-text`: Headings and body text.
- **Text Secondary** (`#475569`) · `--color-text-secondary`: Muted text, captions, placeholders.

### Borders & Surfaces
- **Border** (`#f8f8f8`) · `--color-border`: Dividers, outlines, input borders.

### Full Extracted Palette

| # | Hex | CSS Variable | Role | Area | Contrast |
|---|---|---|---|---|---|
| 1 | `#ffffff` | `--palette-1` | block | large | text-dark |
| 2 | `#000000` | `--palette-2` | button | large | text-light |
| 3 | `#d4af37` | `--palette-3` | text-accent | large | text-dark |
| 4 | `#a87c11` | `--palette-4` | text-accent | large | text-light |
| 5 | `#0f172a` | `--palette-5` | text-accent | medium | text-light |
| 6 | `#475569` | `--palette-6` | text-accent | medium | text-light |
| 7 | `#10b981` | `--palette-7` | button | small | text-dark |
| 8 | `#94a3b8` | `--palette-8` | text-accent | small | text-dark |
| 9 | `#2b579a` | `--palette-9` | badge | small | text-light |
| 10 | `#217346` | `--palette-10` | badge | small | text-light |
| 11 | `#b7472a` | `--palette-11` | badge | small | text-light |
| 12 | `#0078d4` | `--palette-12` | badge | small | text-light |
| 13 | `#e5c158` | `--palette-13` | text-accent | small | text-dark |
| 14 | `#f8f8f8` | `--palette-14` | badge | small | text-dark |

## 3. Typography Rules

- **Heading Font:** `Outfit`, sans-serif
- **Body Font:** `Plus Jakarta Sans`, sans-serif

### Type Hierarchy

| Role | Font | Size | Weight | Line Height | Letter Spacing |
|---|---|---|---|---|---|
| H1 | Outfit | 52px | 800 | 59.8px | -1.56px |
| H2 | Outfit | 20px | 500 | 28px | -0.2px |
| H3 | Outfit | 20px | 700 | 25px | normal |
| H4 | Outfit | 16px | 700 | 20px | normal |
| Body | Plus Jakarta Sans | 20px | 600 | 29px | normal |

### Type Scale

| Token | Size | Suggested Usage |
|---|---|---|
| Display | `52px` | headings |
| H1 | `48px` | headings |
| H2 | `40px` | headings |
| H3 | `29.6px` | headings |
| H4 | `28px` | headings |
| Body L | `24px` | body / supporting text |
| Body | `21.6px` | body / supporting text |
| Small | `20px` | body / supporting text |
| XS | `18.4px` | body / supporting text |
| Caption | `17.6px` | body / supporting text |

## 4. Component Stylings

### Primary Button

```css
.btn-primary {
  background: transparent;
  color: #475569;
  border-radius: 0px;
  padding: 4px 0px;
  font-size: 14.4px;
  font-weight: 500;
  border: none;
  cursor: pointer;
}
```

### Pill Button

```css
.btn-pill {
  background: #0f172a;
  color: #ffffff;
  border-radius: 9999px;
  padding: 10px 20px;
  font-size: 13.6px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}
```

### Filled Button

```css
.btn-filled {
  background: #0f172a;
  color: #ffffff;
  border-radius: 20px;
  padding: 14px 28px;
  font-size: 15.2px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}
```

### Filled Button 2

```css
.btn-filled-2 {
  background: #ffffff;
  color: #0f172a;
  border-radius: 20px;
  padding: 14px 28px;
  font-size: 15.2px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.5);
  cursor: pointer;
}
```

### Filled Button 3

```css
.btn-filled-3 {
  background: #ffffff;
  color: #475569;
  border-radius: 50px;
  padding: 0px 0px;
  font-size: 16px;
  font-weight: 400;
  border: 1px solid rgba(255, 255, 255, 0.5);
  cursor: pointer;
}
```

### Filled Button 4

```css
.btn-filled-4 {
  background: #ffffff;
  color: #0f172a;
  border-radius: 50px;
  padding: 0px 0px;
  font-size: 13.3333px;
  font-weight: 400;
  border: 1px solid rgba(255, 255, 255, 0.5);
  cursor: pointer;
}
```

### Card

```css
.card {
  background: #ffffff;
  border-radius: 32px;
  padding: 60px;
  box-shadow: rgba(0, 0, 0, 0.05) 0px 12px 32px 0px, rgba(255, 255, 255, 0.9) 0px 1px 0px 0px inset;
}
```

## 5. Layout Principles

- **Base spacing unit:** `6px` — use multiples (12px, 18px, 24px, etc.)

### Spacing Scale (extracted from real elements)

| Token | Value | Role |
|---|---|---|
| spacing-1 | `6px` | element |
| spacing-2 | `14px` | element |
| spacing-3 | `24px` | card |
| spacing-4 | `16px` | element |
| spacing-5 | `32px` | card |
| spacing-6 | `40px` | card |
| spacing-7 | `8px` | element |
| spacing-8 | `4px` | element |

### Border Radius Scale

| Token | Value | Element |
|---|---|---|
| radius-card | `50px` | card |
| radius-card | `20px` | card |
| radius-button | `12px` | button |
| radius-card | `32px` | card |
| radius-button | `6px` | button |
| radius-button | `8px` | button |

## 6. Depth & Elevation

| Level | Shadow | Usage |
|---|---|---|
| Mid | `rgba(0, 0, 0, 0.03) 0px 4px 12px 0px, rgba(255, 255, 255, 0.9) 0px 1px 0px 0px i...` | Dropdowns, popovers |
| Deep | `rgba(0, 0, 0, 0.05) 0px 12px 32px 0px, rgba(255, 255, 255, 0.9) 0px 1px 0px 0px ...` | Hero sections, deep layers |
| Mid | `rgba(0, 0, 0, 0.03) 0px 4px 12px 0px` | Dropdowns, popovers |
| Deep | `rgba(0, 0, 0, 0.06) 0px 24px 64px 0px` | Hero sections, deep layers |
| High | `rgba(0, 0, 0, 0.08) 0px 4px 20px 0px` | Modals, floating elements |

> **Note:** This site uses chromatic (color-tinted) shadows rather than pure black — this is a deliberate brand choice that adds warmth to elevation.

## 7. Do's and Don'ts

### Do
- Use `#f5f7fa` as the primary background color
- Use `Outfit` for all headings and `Plus Jakarta Sans` for body text
- Use `#d4af37` as the single dominant accent/CTA color
- Maintain `6px` as the base spacing unit — all gaps should be multiples
- Use rounded corners (`50px`+) consistently for all interactive elements
- Embrace bold color combinations — playful energy is the point
- Apply the shadow system for elevation — use the extracted shadow values
- Use weight 800 for headings to match the brand's typographic voice

### Don't
- Don't use colors outside the extracted palette without justification
- Don't substitute Outfit/Plus Jakarta Sans with generic alternatives
- Don't use irregular spacing — stick to 6px grid
- Don't use dark/black backgrounds — this is a light-themed design
- Don't use sharp corners — they feel hostile in this rounded design language
- Don't use pure black (#000000) for text — use `#475569` instead
- Don't add decorative elements not present in the original design — no badges, ribbons, banners, or ornaments unless the source site uses them
- Don't invent UI patterns the source site doesn't have — if the original has no NEW badge, don't add one just because a red is in the palette

## 8. Responsive Behavior

| Breakpoint | Width | Notes |
|---|---|---|
| Mobile | < 640px | Single column, stack sections, reduce font sizes ~80% |
| Tablet | 640–1024px | 2-column where appropriate, maintain spacing ratios |
| Desktop | 1024–1440px | Full layout as designed |
| Wide | > 1440px | Max-width container, center content |

- Touch targets: minimum 44×44px on mobile
- Maintain 6px base unit across breakpoints — only scale multipliers

## 9. Agent Prompt Guide

### Quick Color Reference

```
Background:  #f5f7fa
Text:        #475569
Accent:      #d4af37
Secondary:   #a87c11
Border:      #f8f8f8
```

### Example Prompts

1. "Build a hero section with a `#f5f7fa` background, `Outfit` heading in `#475569`, and a `#d4af37` CTA button with 9999px radius."
2. "Create a pricing card using background `#000000`, border `#f8f8f8`, `Plus Jakarta Sans` for text, and 18px padding."
3. "Design a navigation bar — `#f5f7fa` background, `#475569` links, `#d4af37` for active state."
4. "Build a feature grid with 3 columns, 18px gap, each card using the card component style."
5. "Create a footer with `#475569` background, `#f5f7fa` text, and 12px padding."

### Iteration Guide

1. Start with layout structure (sections, grid, spacing)
2. Apply colors from the palette — background first, then text, then accents
3. Set typography — font families, sizes from the type scale, weights
4. Add components — buttons, cards, inputs using the specs above
5. Apply border-radius consistently across all elements
6. Add shadows for depth — use the extracted shadow values, not defaults
7. Check responsive behavior — test mobile and tablet layouts
8. Final pass — verify all colors match, spacing is consistent, fonts are correct

## 10. CSS Custom Properties

> 35 custom properties extracted from `:root` / `html` stylesheets.

### Color Variables

| Variable | Value |
|---|---|
| `--bg-primary` | `#F5F7FA` |
| `--bg-secondary` | `#FFFFFF` |
| `--glass-bg` | `rgba(255, 255, 255, 0.55)` |
| `--glass-bg-hover` | `rgba(255, 255, 255, 0.75)` |
| `--glass-border` | `rgba(255, 255, 255, 0.5)` |
| `--glass-border-hover` | `rgba(255, 255, 255, 0.75)` |
| `--glass-highlight` | `rgba(255, 255, 255, 0.9)` |
| `--text-primary` | `#0F172A` |
| `--text-secondary` | `#475569` |
| `--text-tertiary` | `#94A3B8` |
| `--accent-blue` | `#D4AF37` |
| `--accent-cyan` | `#E5C158` |
| `--accent-violet` | `#C59B27` |
| `--accent-indigo` | `#A87C11` |
| `--ambient-glow` | `rgba(212, 175, 55, 0.05)` |
| `--shadow-color` | `rgba(0, 0, 0, 0.02)` |
| `--shadow-1` | `0 4px 12px rgba(0, 0, 0, 0.03)` |
| `--shadow-2` | `0 12px 32px rgba(0, 0, 0, 0.05)` |
| `--shadow-3` | `0 24px 64px rgba(0, 0, 0, 0.06)` |
| `--shadow-4` | `0 40px 120px rgba(0, 0, 0, 0.08)` |

### Spacing Variables

| Variable | Value |
|---|---|
| `--radius-full` | `9999px` |
| `--radius-large` | `32px` |
| `--radius-medium` | `20px` |
| `--radius-small` | `12px` |
| `--max-width` | `1400px` |
| `--header-height` | `80px` |

### Typography Variables

| Variable | Value |
|---|---|
| `--font-heading` | `'Outfit', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif` |
| `--font-body` | `'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif` |

### Other Variables

| Variable | Value |
|---|---|
| `--accent-blue-rgb` | `212, 175, 55` |
| `--accent-cyan-rgb` | `229, 193, 88` |
| `--accent-violet-rgb` | `197, 155, 39` |
| `--accent-indigo-rgb` | `168, 124, 17` |
| `--transition-spring` | `all 0.6s cubic-bezier(0.16, 1, 0.3, 1)` |
| `--transition-smooth` | `all 0.3s cubic-bezier(0.25, 1, 0.5, 1)` |
| `--transition-fast` | `all 0.15s ease` |
