\## 1. Core Identity \& Atmosphere

\- \*\*Vibe:\*\* Premium, tactile, and approachable. Merges Airbnb’s friendly rounded geometry with modern high-contrast profile aesthetics.

\- \*\*Mobile First:\*\* All dimensions use a `12px` base grid. Touch targets are minimum `48px`.

\- \*\*Elevation Philosophy:\*\* Use \*\*smooth light shadows\*\*. Never use pure black shadows on white surfaces. Use tinted/chromatic shadows to maintain warmth and clarity on small screens.

\- \*\*Shape Language:\*\* Generous rounding. Cards are `24px`, buttons are pill-shaped (`100px`), stats containers are `16px`. No sharp corners.



\## 2. Color Palette \& Roles



| Token             | Light Value | Dark Value   | Role                              |

| :---------------- | :---------- | :----------- | :-------------------------------- |

| `--bg-surface`    | `#FFFFFF`   | `#1A1A1A`    | Card background                   |

| `--bg-secondary`  | `#F7F7F7`   | `#2C2C2E`    | Stats row / Header bg             |

| `--text-primary`  | `#222222`   | `#FFFFFF`    | Name, Button text (dark mode)     |

| `--text-secondary`| `#6A6A6A`   | `#A0A0A0`    | Role title, stat labels           |

| `--accent-cta`    | `#000000`   | `#FFFFFF`    | Primary "Get in Touch" button     |

| `--status-online` | `#34C759`   | `#34C759`    | Active status indicator           |

| `--border-subtle` | `#E5E5E5`   | `#3A3A3C`    | Stat dividers, card borders       |

| `--brand-accent`  | `#E00B41`   | `#FF385C`    | Verification badges, highlights   |



\## 3. Typography Scale (Mobile Optimized)

\*Font Family: 'Airbnb Cereal VF', 'Inter', sans-serif\*



| Element      | Size  | Weight | Line Height | Tracking | Notes                          |

| :----------- | :---- | :----- | :---------- | :------- | :----------------------------- |

| \*\*Name\*\*     | 18px  | 700    | 24px        | -0.3px   | Tight tracking for premium feel|

| \*\*Role/Title\*\*| 14px | 400    | 20px        | normal   | Muted color                    |

| \*\*Stat Value\*\*| 15px | 600    | 20px        | normal   | High contrast number           |

| \*\*Stat Label\*\*| 11px | 500    | 16px        | +0.5px   | Uppercase or muted             |

| \*\*Button Text\*\*| 15px| 600    | 20px        | normal   | Centered, pill button          |

| \*\*Badge/Tag\*\* | 12px | 600    | 16px        | normal   | Verified checkmark label       |



\## 4. Component Specifications



\### Profile Card Container

```css

.card-profile {

&#x20; background: var(--bg-surface);

&#x20; border-radius: 24px; /\* radius-xxlarge \*/

&#x20; padding: 16px;       /\* spacing-unit \*/

&#x20; width: 100%;

&#x20; max-width: 340px;    /\* Optimal mobile reading width \*/

&#x20; 

&#x20; /\* Smooth Light Shadow - Tinted \*/

&#x20; box-shadow: 

&#x20;   0px 2px 4px rgba(0, 0, 0, 0.02),

&#x20;   0px 8px 16px rgba(0, 0, 0, 0.04);

&#x20;   

&#x20; transition: transform 0.2s ease, box-shadow 0.2s ease;

}



/\* Dark Mode Override \*/

.dark .card-profile {

&#x20; box-shadow: 

&#x20;   0px 2px 4px rgba(0, 0, 0, 0.2),

&#x20;   0px 8px 16px rgba(0, 0, 0, 0.3);

}



Depth \& Elevation System (Smooth Light Shadows)

Level

CSS Shadow Value

Usage

Resting

0px 2px 4px rgba(0,0,0,0.02), 0px 8px 16px rgba(0,0,0,0.04)

Default Card State

Pressed

0px 1px 2px rgba(0,0,0,0.05)

Button Active State

Floating

0px 12px 24px rgba(0,0,0,0.06)

Modal / Popover

Dark Resting

0px 2px 4px rgba(0,0,0,0.2), 0px 8px 16px rgba(0,0,0,0.3)

Dark Mode Card

⚠️ Critical: Never use rgba(0,0,0,0.5) or higher opacity shadows on white cards. It creates a "dirty" look. Stick to 0.02–0.06 range for light mode.



Responsive Behavior

Breakpoint

Width

Behavior

Mobile S

< 360px

Single column, avatar 48px, stats stack if needed

Mobile L

360–420px

Standard layout as designed

Tablet

> 768px

2-column grid, increase padding to 24px





