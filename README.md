<div align="left">
    <img src="screenshots\TE Canvas Logo & Title.png" alt="App Logo" width="500">
</div>

# Text Editor on Canvas - Android App

#### An Android app to create, edit or modify text upon a square canvas. It can be used to create personalized text-based designs or to write some Quotes. </br>

This is a Single Activity App :- &nbsp; ***[MainActivity.kt](app/src/main/java/com/ayushxp/texteditoroncanvasapp/MainActivity.kt)** &nbsp;(~1300 lines)*

- <p><b>Download & Test the app in your phone ⬇️</b> </br>

    - Get App directly to your Email account via Firebase App distribution :-
      https://appdistribution.firebase.google.com/testerapps/1:226247968382:android:d30be6b18985dbf0c23a2f/releases/11rh6hgdk0dv0?utm_source=firebase-console

    - Download Apk via github releases :-
      https://github.com/ayushpadlekar/Text_Editor_On_Canvas/releases/tag/v1.0.0

</p>

</br>

> [!NOTE]
> *- This app was made as an assignment for an insternship. </br> - The main challenge of the assignment was : to make the Text stay inside of the square canvas. It should not go outside the boundary whenever we drag or move the text.* </br>

> <ins><b> How I solved this challenge</b></ins> :- </br>
> <p> - I understood the problem carefully and analyzed what we have & what can be done. </br>
> - As we know, the square canvas have it's own coordinates (starting from 0,0 in the top-left). And the text has its co-ordinates position upon that canvas. When we drag the text, the touch-pointer-gesture assigns new X & Y co-ordinates to the text constantly. </br>
> 
> • So, I used my logic & applied constraints on each boundary sides of the square canvas that : </br>
> &nbsp; - if new-coordinates of text try to go outside the square boundary coordinates, then new-coordinates = boundary coordinates. </br>
> &nbsp; - which means the new-coordinates cannot go beyond the boundary-coordinates. </br>
> - Example code snippet :
> <pre>
>   if (new-coordinate < boundary-coordinate) {
>       new-coordinate = boundary-coordinate
>   }
> </pre> </p>

</br>

## Features 💡

* Add and edit text on a square canvas.
* Drag/Move text anywhere upon canvas.
* Customize fonts, size, and color of text.
* Format text - bold, italic, underline, strikethrough.
* Change canvas color - white/black/gray.
* Undo & Redo functionality.
* Dark Mode option.


## Screenshots 📸

<table>
<tr align = "center">
    <td>Light Mode ☀️</td>
    <td>Dark Mode 🌙</td>
</tr>
<tr>
    <td><img src="screenshots\TE Canvas UI - Light mode.png" width=300></td>
    <td><img src="screenshots\TE Canvas UI - Dark mode.png" width=300></td>
</tr>
</table>


## Tech Stack 🛠️

* **Jetpack Compose :** Developed easy to use, modern & sleek UI design. Extensively used for designing the Title of the App.

* **Kotlin :** Utilized as the primary programming language for the app. Created dynamic features with mutable states & undo redo stacks management.

* **Android Studio :** IDE for developing this app.


## Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes. Feel free to open issues.
