# Design System Inspired by Airbnb

> Auto-extracted from `https://www.airbnb.com/` on 2026-07-13

## 1. Visual Theme & Atmosphere

Friendly, approachable design with rounded shapes and generous whitespace.

The hero section leads with "Airbnb homepage" followed by "HomesHomesNEWNEWExperiencesExperiences, newNEWNEWServicesServices, newWhereWhenAdd datesWhoAdd guest".

**Key Characteristics:**
- Airbnb Cereal VF as the heading font (custom web font loaded via @font-face)
- Airbnb Cereal VF as the body font for all running text
- Heading weight 700
- Light/white background (#ffffff) as the primary canvas
- Primary accent `#e00b41` used for CTAs and brand highlights
- 3 shadow level(s) detected — tinted shadows
- Rounded corners (20px+) creating a friendly, approachable feel
- Tags: light, rounded, accented, sans-serif

## 2. Color Palette & Roles

### Primary
- **Primary Accent** (`#e00b41`) · `--color-primary`: Brand color, CTA backgrounds, link text, interactive highlights.
- **Secondary Accent** (`#ff385c`) · `--color-secondary`: Secondary brand, hover states, complementary highlights.
- **Background** (`#ffffff`) · `--color-bg`: Page background, primary canvas.

### Text
- **Text Primary** (`#222222`) · `--color-text`: Headings and body text.
- **Text Secondary** (`#b0b0b0`) · `--color-text-secondary`: Muted text, captions, placeholders.

### Borders & Surfaces
- **Border** (`#dddddd`) · `--color-border`: Dividers, outlines, input borders.

### Full Extracted Palette

| # | Hex | CSS Variable | Role | Area | Contrast |
|---|---|---|---|---|---|
| 1 | `#dddddd` | `--palette-1` | block | large | text-dark |
| 2 | `#ffffff` | `--palette-2` | block | large | text-dark |
| 3 | `#b0b0b0` | `--palette-3` | button | medium | text-dark |
| 4 | `#ebebeb` | `--palette-4` | block | medium | text-dark |
| 5 | `#f2f2f2` | `--palette-5` | badge | medium | text-dark |
| 6 | `#e00b41` | `--palette-6` | button | small | text-light |
| 7 | `#ff385c` | `--palette-7` | text-accent | small | text-light |
| 8 | `#222222` | `--palette-8` | badge | small | text-light |

## 3. Typography Rules

- **Heading Font:** `Airbnb Cereal VF` (web font)
- **Body Font:** `Airbnb Cereal VF` (web font)

### Type Hierarchy

| Role | Font | Size | Weight | Line Height | Letter Spacing |
|---|---|---|---|---|---|
| H1 | Airbnb Cereal VF | 28px | 700 | 40.04px | normal |
| H2 | Airbnb Cereal VF | 14px | 400 | 20.02px | normal |
| H3 | Airbnb Cereal VF | 14px | 500 | 18px | normal |

### Type Scale

| Token | Size | Suggested Usage |
|---|---|---|
| Display | `28px` | headings |
| H1 | `20px` | headings |
| H2 | `16px` | headings |
| H3 | `14px` | headings |
| H4 | `13px` | headings |
| Body L | `12px` | body / supporting text |
| Body | `11px` | body / supporting text |
| Small | `8px` | body / supporting text |

## 4. Component Stylings

### Primary Button

```css
.btn-primary {
  background: transparent;
  color: #222222;
  border-radius: 32px;
  padding: 0px 0px;
  font-size: 14px;
  font-weight: 400;
  border: none;
  cursor: pointer;
}
```

### Ghost Button

```css
.btn-ghost {
  background: transparent;
  color: #222222;
  border-radius: 50px;
  padding: 0px 0px;
  font-size: 14px;
  font-weight: 400;
  border: none;
  cursor: pointer;
}
```

### Ghost Button 2

```css
.btn-ghost-2 {
  background: transparent;
  color: #222222;
  border-radius: 20px;
  padding: 11px 12px;
  font-size: 14px;
  font-weight: 500;
  border: none;
  cursor: pointer;
}
```

### Filled Button

```css
.btn-filled {
  background: #f2f2f2;
  color: #222222;
  border-radius: 50px;
  padding: 0px 0px;
  font-size: 14px;
  font-weight: 400;
  border: none;
  cursor: pointer;
}
```

## 5. Layout Principles

- **Base spacing unit:** `12px` — use multiples (24px, 36px, 48px, etc.)

### Spacing Scale (extracted from real elements)

| Token | Value | Role |
|---|---|---|
| spacing-1 | `12px` | element |
| spacing-2 | `2px` | element |
| spacing-3 | `5.5px` | element |
| spacing-4 | `4px` | element |
| spacing-5 | `18.375px` | element |
| spacing-6 | `15px` | element |
| spacing-7 | `3px` | element |
| spacing-8 | `10px` | element |

### Border Radius Scale

| Token | Value | Element |
|---|---|---|
| radius-card | `20px` | card |
| radius-card | `50px` | card |
| radius-button | `14px` | button |
| radius-button | `8px` | button |
| radius-card | `32px` | card |
| radius-pill | `100px` | pill |

## 6. Depth & Elevation

| Level | Shadow | Usage |
|---|---|---|
| Low | `rgba(0, 0, 0, 0.02) 0px 0px 0px 1px, rgba(0, 0, 0, 0.04) 0px 2px 6px 0px, rgba(0...` | Cards, subtle elevation |
| Low | `rgba(60, 77, 107, 0.25) 0px 0.953955px 1.90791px 0px, rgba(60, 77, 107, 0.25) 0p...` | Cards, subtle elevation |
| Low | `rgba(0, 0, 0, 0.02) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 8px 24px 0px` | Cards, subtle elevation |

> **Note:** This site uses chromatic (color-tinted) shadows rather than pure black — this is a deliberate brand choice that adds warmth to elevation.

## 7. Do's and Don'ts

### Do
- Use `#ffffff` as the primary background color
- Use `Airbnb Cereal VF` for all headings and `Airbnb Cereal VF` for body text
- Use `#e00b41` as the single dominant accent/CTA color
- Maintain `12px` as the base spacing unit — all gaps should be multiples
- Use rounded corners (`20px`+) consistently for all interactive elements
- Apply the shadow system for elevation — use the extracted shadow values
- Use weight 700 for headings to match the brand's typographic voice

### Don't
- Don't use colors outside the extracted palette without justification
- Don't substitute Airbnb Cereal VF/Airbnb Cereal VF with generic alternatives
- Don't use irregular spacing — stick to 12px grid
- Don't use dark/black backgrounds — this is a light-themed design
- Don't use sharp corners — they feel hostile in this rounded design language
- Don't use pure black (#000000) for text — use `#222222` instead
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
- Maintain 12px base unit across breakpoints — only scale multipliers

## 9. Agent Prompt Guide

### Quick Color Reference

```
Background:  #ffffff
Text:        #222222
Accent:      #e00b41
Secondary:   #ff385c
Border:      #dddddd
```

### Example Prompts

1. "Build a hero section with a `#ffffff` background, `Airbnb Cereal VF` heading in `#222222`, and a `#e00b41` CTA button with 50px radius."
2. "Create a pricing card using background `#ffffff`, border `#dddddd`, `Airbnb Cereal VF` for text, and 36px padding."
3. "Design a navigation bar — `#ffffff` background, `#222222` links, `#e00b41` for active state."
4. "Build a feature grid with 3 columns, 36px gap, each card using the card component style."
5. "Create a footer with `#222222` background, `#ffffff` text, and 24px padding."

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

> 390 custom properties extracted from `:root` / `html` stylesheets.

### Color Variables

| Variable | Value |
|---|---|
| `--elevation-high-box-shadow` | `0 8px 28px rgba(0,0,0,0.28)` |
| `--elevation-high-border` | `1px solid rgba(0,0,0,0.04)` |
| `--elevation-primary-box-shadow` | `0 6px 20px rgba(0,0,0,0.2)` |
| `--elevation-primary-border` | `1px solid rgba(0,0,0,0.04)` |
| `--elevation-secondary-box-shadow` | `0 6px 16px rgba(0,0,0,0.12)` |
| `--elevation-secondary-border` | `1px solid rgba(0,0,0,0.04)` |
| `--elevation-sharp-edge-background` | `rgba(0,0,0,0.08)` |
| `--elevation-tertiary-box-shadow` | `0 2px 4px rgba(0,0,0,0.18)` |
| `--elevation-tertiary-border` | `1px solid rgba(0,0,0,0.08)` |
| `--elevation-elevation0-box-shadow` | `0px 0px 0px 1px #DDDDDD inset` |
| `--elevation-elevation1-box-shadow` | `0px 0px 0px 1px rgba(0,0,0,0.02),0px 2px 4px 0px rgba(0,0,0,0.16)` |
| `--elevation-elevation2-box-shadow` | `0px 0px 0px 1px rgba(0,0,0,0.02),0px 2px 6px 0px rgba(0,0,0,0.04),0px 4px 8px 0px rgba(0,0,0,0.10)` |
| `--elevation-elevation3-box-shadow` | `0px 0px 0px 1px rgba(0,0,0,0.02),0px 8px 24px 0px rgba(0,0,0,0.10)` |
| `--elevation-elevation4-box-shadow` | `0px 0px 0px 1px rgba(0,0,0,0.02),0px 4px 8px 0px rgba(0,0,0,0.08),0px 12px 30px 0px rgba(0,0,0,0.12)` |
| `--elevation-elevation5-box-shadow` | `0px 0px 0px 1px rgba(0,0,0,0.02),0px 6px 8px 0px rgba(0,0,0,0.10),0px 16px 56px 0px rgba(0,0,0,0.18)` |
| `--palette-black` | `#000000` |
| `--palette-hof` | `#222222` |
| `--palette-foggy` | `#6A6A6A` |
| `--palette-bobo` | `#B0B0B0` |
| `--palette-deco` | `#DDDDDD` |
| `--palette-bebe` | `#EBEBEB` |
| `--palette-faint` | `#F7F7F7` |
| `--palette-white` | `#FFFFFF` |
| `--palette-arches` | `#C13515` |
| `--palette-arches2` | `#B32505` |
| `--palette-arches12` | `#FFF8F6` |
| `--palette-capiz` | `#F7F6F2` |
| `--palette-hapuna` | `#F5F1EA` |
| `--palette-mykonou5` | `#428BFF` |
| `--palette-ondo` | `#E07912` |
| ... | *(195 more)* |

### Spacing Variables

| Variable | Value |
|---|---|
| `--corner-radius-tiny4px-border-radius` | `4px` |
| `--corner-radius-small8px-border-radius` | `8px` |
| `--corner-radius-medium12px-border-radius` | `12px` |
| `--corner-radius-large16px-border-radius` | `16px` |
| `--corner-radius-xlarge20px-border-radius` | `20px` |
| `--corner-radius-xxlarge24px-border-radius` | `24px` |
| `--corner-radius-xxlarge28px-border-radius` | `28px` |
| `--corner-radius-xxxlarge32px-border-radius` | `32px` |
| `--motion-springs-fast-source-mass` | `1px` |
| `--motion-springs-fast-source-damping` | `35px` |
| `--motion-springs-fast-source-stiffness` | `300px` |
| `--motion-springs-standard-source-mass` | `1px` |
| `--motion-springs-standard-source-damping` | `26px` |
| `--motion-springs-standard-source-stiffness` | `175px` |
| `--motion-springs-medium-bounce-source-mass` | `1px` |
| `--motion-springs-medium-bounce-source-damping` | `18.5px` |
| `--motion-springs-medium-bounce-source-stiffness` | `175px` |
| `--motion-springs-fast-bounce-source-mass` | `1px` |
| `--motion-springs-fast-bounce-source-damping` | `22px` |
| `--motion-springs-fast-bounce-source-stiffness` | `250px` |
| ... | *(24 more)* |

### Typography Variables

| Variable | Value |
|---|---|
| `--typography-font-family-cereal-font-family` | `'Airbnb Cereal VF','Circular',-apple-system,'BlinkMacSystemFont','Roboto','Helvetica Neue',sans-serif` |
| `--typography-special-display-medium_40_44-font-size` | `2.5rem` |
| `--typography-special-display-medium_40_44-line-height` | `2.75rem` |
| `--typography-special-display-medium_40_44-letter-spacing` | `normal` |
| `--typography-special-display-medium_40_44-font-weight` | `600` |
| `--typography-special-display-medium_48_54-font-size` | `3rem` |
| `--typography-special-display-medium_48_54-line-height` | `3.375rem` |
| `--typography-special-display-medium_48_54-letter-spacing` | `normal` |
| `--typography-special-display-medium_48_54-font-weight` | `600` |
| `--typography-special-display-medium_60_68-font-size` | `3.75rem` |
| `--typography-special-display-medium_60_68-line-height` | `4.25rem` |
| `--typography-special-display-medium_60_68-letter-spacing` | `normal` |
| `--typography-special-display-medium_60_68-font-weight` | `600` |
| `--typography-special-display-medium_72_74-font-size` | `4.5rem` |
| `--typography-special-display-medium_72_74-line-height` | `4.625rem` |
| `--typography-special-display-medium_72_74-letter-spacing` | `normal` |
| `--typography-special-display-medium_72_74-font-weight` | `600` |
| `--typography-titles-semibold_14_18-font-size` | `0.875rem` |
| `--typography-titles-semibold_14_18-line-height` | `1.125rem` |
| `--typography-titles-semibold_14_18-letter-spacing` | `normal` |
| ... | *(80 more)* |

### Other Variables

| Variable | Value |
|---|---|
| `--motion-springs-fast-duration` | `451.75438596491193ms` |
| `--motion-springs-fast-easing` | `linear(0,0.18557241650572898,0.46530560393651493,0.6823338821577483,0.8223254801509006,0.9049744175651648,0.951288850000914,0.9763638545339052,0.9896118636450829,0.9964846505475399,1)` |
| `--motion-springs-standard-duration` | `583.7719298245607ms` |
| `--motion-springs-standard-easing` | `linear(0,0.15794349142280711,0.4146686698630492,0.6303103850844771,0.7802275692100804,0.8751011408890221,0.9317564666924485,0.9642434451985746,0.9823049252758026,0.992097579596505,0.9972943925635941,1)` |
| `--motion-springs-medium-bounce-duration` | `574.1228070175433ms` |
| `--motion-springs-medium-bounce-easing` | `linear(0,0.17056804830171035,0.47921259292635127,0.749704997553311,0.9261583179716212,1.0149357719696455,1.0442328379057395,1.042269832870079,1.028981085732054,1.0152861473492045,1.0054622129208994,1)` |
| `--motion-springs-fast-bounce-duration` | `449.12280701754327ms` |
| `--motion-springs-fast-bounce-easing` | `linear(0,0.25484239226416866,0.643483807710504,0.9061742021274407,1.0208040643586513,1.043750765143047,1.0303012036555117,1.0119725530453332,1)` |
| `--motion-springs-slow-duration` | `745.6140350877179ms` |
| `--motion-springs-slow-easing` | `linear(0,0.1726495179466309,0.44139132340393467,0.6575338740242772,0.8021357455779029,0.890693569261087,0.9421755177398626,0.9710919896728034,0.9869420351097642,0.9954729340379553,1)` |
| `--motion-springs-slow-bounce-duration` | `762.2807017543847ms` |
| `--motion-springs-slow-bounce-easing` | `linear(0,0.17157063121773947,0.4812770425544863,0.7518340186858384,0.9276145377206974,1.0155374835651005,1.0441834344763297,1.0418987538382922,1.028565879063093,1.0149848580762686,1.005322404392434,1)` |
| `--motion-standard-curve-animation-timing-function` | `cubic-bezier(0.2,0,0,1)` |
| `--motion-enter-curve-animation-timing-function` | `cubic-bezier(0.1,0.9,0.2,1)` |
| `--motion-exit-curve-animation-timing-function` | `cubic-bezier(0.4,0,1,1)` |
| ... | *(6 more)* |
