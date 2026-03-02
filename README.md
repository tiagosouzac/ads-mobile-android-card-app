# 💳 Android Credit Card App

A clean and functional Android application developed as a technical challenge. The app simulates a credit card interface where a visual card preview updates in real time as the user fills in the form fields below it.

## 🚀 Features

* **Real-time Preview:** The card display at the top of the screen reflects every keystroke — number, holder name, expiry date, and CVV are updated instantly.
* **Card Number Mask:** Automatically inserts a space every 4 digits (e.g., `1234 5678 9012 3456`).
* **Expiry Date Mask:** Month and year fields follow the `MM/AA` pattern, and the month field auto-advances focus to the year field after 2 digits.
* **Month Validation:** Prevents the user from entering a month greater than 12.
* **Form Validation:** Submission is blocked if the card number does not have 16 digits or the holder name has fewer than 3 characters.
* **Card Flip — Challenge 1:** Uses a custom flip animation (`ObjectAnimator`) to toggle between the front and back of the card. The card rotates to the **back** when the user focuses on the CVV field, and returns to the **front** for all other fields.
* **Dynamic Brand Detection — Challenge 2:** As the user types the card number, the app identifies the card brand in real time based on the leading digits and updates the logo accordingly:
  * **Visa** — starts with `4`
  * **Mastercard** — starts with `51–55` or `2221–2720`
  * **Unknown** — logo is hidden

## 🛠️ Tech Stack

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI Layout:** `ConstraintLayout` + `ScrollView`
* **Components:** `CardView`, `FrameLayout`, `TextInputLayout`, `TextInputEditText`, `ImageView`, `TextView`
* **Animation:** `ObjectAnimator` (card flip — rotate Y axis)
* **Architecture:** View-based with `TextWatcher` listeners and `OnFocusChangeListener`

## 📥 How to Run

1. **Clone the repository:**
```bash
git clone https://github.com/tiagosouzac/ads-mobile-android-card-app.git
```

2. **Open in Android Studio:**
   * Go to `File > Open` and select the project folder.

3. **Sync Gradle:**
   * Wait for the project to sync and download the necessary dependencies.

4. **Run the app:**
   * Select an Emulator or a physical device and click the **Run** button (green play icon).

## 📝 Implementation Details

### Challenge 1 — Card Flip (Dynamic UI)

Instead of a `ViewFlipper`, the flip effect was built using two `ObjectAnimator` instances that animate the `rotationY` property:

1. The visible face rotates from `0°` to `90°` (card goes edge-on).
2. Once edge-on (invisible to the user), the faces swap — the current face is hidden (`GONE`) and the new face becomes visible.
3. The new face then rotates from `-90°` to `0°`, completing the flip.

A guard flag (`isFlipping`) prevents double-triggers during the animation. Focus listeners on each form field automatically trigger `showFront()` or `showBack()` depending on which field is active.

### Challenge 2 — Dynamic Brand Detection

The `updateCardBrand()` function is called on every `afterTextChanged` event of the card number field. It reads the raw digits (without spaces) and applies the following rules:

| Brand       | Rule                                            |
|-------------|-------------------------------------------------|
| Visa        | First digit is `4`                              |
| Mastercard  | First two digits in range `51–55`, **or** first four digits in range `2221–2720` |
| Unknown     | Anything else — logo is hidden                  |

The card color and logo image update instantly, giving the user immediate visual feedback.

