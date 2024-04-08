# TScripts
A runtime lightweight scripting platform for automation. TScripts uses a custom php/js-like syntax for scripting in-game automation.

## The UI

### Main Panel

![image](https://github.com/Tonic-Box/TScriptsRepository/assets/73169574/c9bd5bc9-06d6-42c8-a66c-251c3da7e9ea)

This is the main UI panel. From here we can create new scripts, create new profiles, toggle logging options, and so on. The + in the upper right adds a new script. The + in the lower right adds a new profile. The Documentation button generates documentation of everything currently implemented and opens it in a text file for you. There is a built-in documentation explorer ui aswell that we will get to later. Its in with the debug tooling.

### Script Editor
![image](https://github.com/Tonic-Box/TScriptsRepository/assets/73169574/d12bc126-cf87-473b-b41a-69b5fe5004c2)

This is the script editor. Here you can edit scripts from the currently selected profile. The **Console** button toggles the output debug console from the bottom of the window. The **Dev Tools** button toggles the developer.debug tooling panal from the right of the window.

**Fully Expanded Window:**

![image](https://github.com/Tonic-Box/TScriptsRepository/assets/73169574/4ff03d0f-93cf-471b-827d-18f03609f94a)

### Dev Tools
* **Control-Flow**: This tab shows a control flow graph of the surrent script and can be refreshed by simply clicking the tab again. Also when a script is running this will update with highlights showing in real time where you are in the control flow during runtime.
* **Variables**: This tab shows a live map of user variables during runtime
* **Runtime**: This tab shows a live map of internal flags of the runtime engine and its current states and so on.
* **Tokens**: This tab shows a list of all the tokens the currently viewed script gets broken down into prior to Lexing. This is mostly a tool forassisting in new language feature development.
* **Documentation**: This tab contains the visual documentation explorer. The documentation is dynamically generated from everything currently implemented at the time.

## Features
* **Next level packet support**
TScripts function api is fully backed by packets. Its also features fancy packet logging to console and chat where you can left click to copy the log. It parses out the values from the buffer and logs it into a function format you can paste right into scripts.

![image](https://github.com/Tonic-Box/TScriptsRepository/assets/73169574/a7e7258e-2e1d-4de7-9547-492e5a323e19)
