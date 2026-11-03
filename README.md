<p align="center" style="margin-bottom:0">
  <img class="logo" src="etc/doc/autumo-ui-fx-logo.png" alt="autumo — logo" width="256" />
</p>

<h1 align="center" style="font-size:3.5em; margin:0px">autumo UI FX</h1>
<br />

## 1. Introduction

autumo UI is a **rapid JavaFX development framework**. It provides the application
shell — lifecycle, installer, i18n (PO gettext), user settings and recent files 
— so that an application is mostly the code that is actually specific to it.

**See a demo here:**
- [https://github.com/autumoswitzerland/autumo-ui-fx-demo](https://github.com/autumoswitzerland/autumo-ui-fx-demo)

### What you can do with it

An application built with autumo UI is a **finished desktop product**, not just a
window:

- **A product shell with an installer.** The installer (license agreement,
  basic settings, …) is configurable and runs on macOS, Windows and Linux/UNIX;
  the application can also run in *portable mode*, without installing anything.
- **A dark, consistent look.** The base theme (CSS) is shipped with the module;
  an application only overrides what it really wants to change.
- **Internationalization.** Texts are resolved from gettext catalogues (PO
  files); the language can be switched at runtime.
- **Product updates.** The update check reads a simple online XML file and
  offers the new version.
- **Standard dialogs.** About, license, check-for-update, registration,
  remove-app-data, information, warning and error dialogs are ready to use and
  can be customized.
- **HTML views, MVC and logging.** Views in FXML with HTML content, the classic
  MVC structure and logging with SLF4J/Log4j2 on every operating system.
- **Full application configuration.** The configuration is handled for macOS,
  Windows and Linux/UNIX, including the OS-specific data directories.

There is also a commercial version of this package that provides additional components:

- **Licensing and registration.** The framework includes a registration view integrated 
  with a fully functional licensing implementation, an online license server platform, 
  and license file management.
- **Common base libraries.** A collection of reusable libraries providing numerous 
  additional foundational components, primarily designed to support the application 
  lifecycle of desktop applications.
- **Application update checks.**

### The key classes

Start by reading the **Javadoc** shipped in the `doc/` folder. The following classes
are key for development:

| Class | Purpose |
| --- | --- |
| `ch.autumo.ui.fx.BaseApp` | The JavaFX application you **subclass**. Lifecycle, configuration, logging, installer, licensing hooks, recent files. |
| `ch.autumo.ui.fx.controller.BaseController` | Base controller for every JavaFX controller that binds controls to FXML views (`*.fxml`): initialization, customization and UI event handling. Delegate work to actions. Also owns the runtime **language menu** (`initializeLanguageMenu(menu)` — one radio item per supported language (text only, no flag icons), plus the Phosphor `translate` glyph on the menu itself, no PNG fallback — , `switchLanguage(lang)`) and calls the overridable `languageChanged()` hook, so an application only re-applies its own texts. |
| `ch.autumo.ui.fx.controller.actions.BaseAction` | Simple action with one `execute(...)` method that has access to the controller, the application and the view. Used for all dialogs and menu/toolbar actions. |
| `ch.autumo.ui.fx.view.BaseView` (`Pane`) | Base view for a JavaFX view; in most cases only a constructor is needed. Applies stylesheet styles. |
| `ch.autumo.ui.fx.view.BaseBorderView` (`BorderPane`) | Border-pane variant of the base view. |
| `ch.autumo.ui.fx.view.BaseAnchorView` (`AnchorPane`) | Anchor-pane variant of the base view. |
| `ch.autumo.ui.fx.model.BaseModel` | Optional model. In this component's design the model is dispensed with, but you are free to implement data mapping/access in a subclass. |
| `ch.autumo.ui.utils.UI` | Set up views with FXML, controllers, key shortcuts, window decoration, macOS menu-bar options; open the standard dialogs (About, HTML, License, Check-for-update, Registration, Remove app-data, customized) and show information/warning/error dialogs. |
| `ch.autumo.ui.utils.Helper` | Configuration file manipulation; loading resources from the base (autumo UI) **and** your application resource base; translating text; loading available versions from a server (update check); creating web views; country data; external resource operations (open file/folder/website); file and folder choosers, file save dialogs. |

### Quickstart

- Subclass `ch.autumo.ui.fx.BaseApp` (see chapter 3).
- Read the Java API documentation in `doc/` (generated with `./gradlew javadoc`).
- Run the **autumo UI FX Demo** project to see a complete application in action: 
  [https://github.com/autumoswitzerland/autumo-ui-fx-demo](https://github.com/autumoswitzerland/autumo-ui-fx-demo)

## 2. Requirements

| Area | Requirement |
| --- | --- |
| Language | **Java 25** (JDK) |
| UI toolkit | **JavaFX 25** (`javafx.controls`, `javafx.fxml`, `javafx.web`, `javafx.swing`) |
| Build | **Gradle 9.7** (the wrapper pins `9.7.0`; always use `./gradlew`) |
| JavaFX in your project | **Must be declared by the application itself** — autumo-ui-fx does not expose JavaFX transitively (see 2.1) |
| Runnable application | autumo-ui-fx is a **library**: it has no `run` task. Use the UI Demo project to start an application |

### 2.1 Setting up your own project

The framework supplies the application shell; JavaFX must be added by your project.
The following `build.gradle` is the complete minimum (compare the UI Demo project):

```groovy
plugins {
    id 'java'
    id 'application'
    id 'org.openjfx.javafxplugin' version '0.1.0'
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

javafx {
    version = '25'
    modules = ['javafx.controls', 'javafx.fxml', 'javafx.web', 'javafx.swing']
}

dependencies {
    implementation 'org.apache.logging.log4j:log4j-api:2.26.1'
    implementation 'org.apache.logging.log4j:log4j-core:2.26.1'
    implementation 'org.apache.logging.log4j:log4j-slf4j2-impl:2.26.1'
    
    implementation 'com.google.code.gson:gson:2.11.0'
    implementation 'org.apache.commons:commons-lang3:3.21.0'
	implementation "net.synedra:validatorfx:1.0.0"
    
    // Code editor component (RSyntaxTextArea + RTextScrollPane, BSD-2-Clause), embedded
    // into JavaFX with a SwingNode. The theme (cfg/edit.xml) ships with the
    // framework; see DefaultCodeEditorView.
    api 'com.fifesoft:rsyntaxtextarea:3.5.0'    
}

application {
    mainClass = 'your.app.App'
}

// Allow JavaFX native access, required for interoperability with Swing/AWT on modern JDKs.
tasks.named('run') {
    jvmArgs '--enable-native-access=javafx.graphics'
}
```

`javafx.swing` is required: the framework's code editor (RSyntaxTextArea) is embedded with
a `SwingNode`. `--enable-native-access=javafx.graphics` keeps JDK 25 from warning about
native access when the About/license panes render their `WebView`.

## 3. Subclassing `BaseApp`

Your application class extends `ch.autumo.ui.fx.BaseApp`. The methods you typically
override:

| Method | Purpose |
| --- | --- |
| `getAppTitle()` | Application/window title. |
| `getAppVersion()` | Version string (used by the update check). |
| `getInternalAppName()` | Internal name used for the application data/log directories. |
| `getManufacturer()` | Manufacturer name (part of the data directory). |
| `getBaseResourcePath()` | Resource root of your application (e.g. `/ch/apps/myapp/`). |
| `getConfigTemplateFile()` | Template application configuration (see `cfg/app.cfg`), read from your resources. |
| `getInMemoryConfigName()` | Name of the in-memory configuration. |
| `isPortableApp()` | **Portable mode**: no install, no logging, no files written — handy during development. |
| `preInitialize(stage, cfg)` | Build the main view here and return the root `Pane`. |
| `initialize(stage, cfg)` | Show the stage and wire up events. |
| `finish(stage)` | Cleanup on shutdown. |

```java
public class MyApp extends BaseApp {

    private static final String BASE = "/ch/apps/myapp/";

    private static final RegistrationVersion regVersion = new RegVersion();

    @Override
    protected Pane preInitialize(Stage stage, Properties cfg) {
        // Main view
        final MainView mainView = new MainView(this, stage);
        // MVC setup
    	final MainModel model = new MainModel(mainView);
    	mainView.setModel(model);
    	final MainController controller = new MainController(this, stage, model, mainView);
    	mainView.setController(controller);        
        // Setup UI, must be made after the main view!
        UI.setup(this, this.getBaseResourcePath(), mainView, "main.fxml");
        return mainView;
    }

    @Override
    protected void initialize(Stage stage, Properties cfg) {
        stage.setResizable(false);
		stage.setMinHeight(720);
		stage.setMinWidth(1080);
        // Show primary stage
        stage.show();
    }

    @Override public String getBaseResourcePath()      { return BASE; }
    @Override public String getConfigTemplateFile()    { return BASE + "cfg/app.cfg"; }
    @Override public String getInMemoryConfigName()    { return "myapp"; }
    @Override public String getAppVersion()            { return "1.0.0"; }
    @Override public String getAppTitle()              { return "My App"; }
    @Override public String getInternalAppName()       { return "MyApp"; }
    @Override protected boolean isPortableApp()        { return false; }

    @Override
    public void finish(Stage stage) {
        // cleanup on shutdown (called from BaseApp.stop())
    }
}
```

> Launch through a small **non-`Application`** class (e.g.
> `Application.launch(MyApp.class, args)`) so the app starts when the JavaFX modules
> are on the classpath rather than the module path.

### 3.2 Splash screen

An application can show a **splash screen** while it starts: a small window with its
logo and a scanner bar below it (a light that sweeps from left to right and back).
**By default no splash screen is shown** - an application activates it explicitly:

```java
@Override
protected boolean isSplashScreenEnabled() {
    return true;                            // no splash screen by default
}

@Override
protected SplashScreen createSplashScreen() {
    return new SplashScreen()
            .setLogo(getClass().getResource("icons/app-256.png"))
            .setJingle(getClass().getResource("sounds/start.wav"))
            .setDuration(Duration.seconds(2));
}
```

The framework shows the screen at the beginning of the start-up and hides it as soon as the
main window is up. Everything follows the standard design of the framework and
can be changed:

| Setting | Standard design |
| --- | --- |
| Background of the window | `#0b0f14` |
| Colour of the frame | `#1e2833` |
| Radius of the rounded corners | 18 px |
| Logo | the framework's logo (220 x 220) |
| Scanner bar | 240 x 8 px, the light sweeps in 2.4 s |

**The display time.** Without a duration the splash screen goes away with the main
window. A fast application would flash it up for a fraction of a second only - with
`setDuration(...)` the splash screen stays at least that long and the main window
comes with it.

The jingle is played with the Java Sound API, so it has to be a sound file the JDK can
play (WAV, AU or AIFF) - the application does **not** need the JavaFX media module for
it.

> The configuration is read in a background thread, so the JavaFX thread stays free and
> the splash screen is really painted while the application loads.

### 3.3 Code editor

The framework's code editor (`ch.autumo.ui.fx.view.DefaultCodeEditorView`) is the
reference component of the framework: a [RSyntaxTextArea](https://bobbylight.github.io/RSyntaxTextArea/)
with the framework's theme (`cfg/edit.xml`), the line-number gutter and dark
scrollbars, embedded into JavaFX with a `SwingNode`. The JSON formatter of the framework
is such an editor.

An application that builds its own editor pane derives from it. One hook is useful:

```java
@Override
protected void textChanged() {
    // The user changed the text in the editor. The call comes from the AWT event
    // dispatch thread, so the JavaFX scene is touched with Platform.runLater(...).
    Platform.runLater(() -> markModified());
}
```

`textChanged()` is called for the changes of the **user** only - a text the application
sets itself (`setText(...)`) does not trigger it.

## 4. Resources

The resources are loaded **relative to your application class** (the `BaseApp`
subclass). If your app class is in the package `ch.apps`, then e.g. the application
stylesheet is looked up at `<resource-path>/ch/apps/style/application.css`.

### 4.1 `cfg`

| Resource | Meaning |
| --- | --- |
| `app.cfg` | **Template for your application configuration.** Read from your resources and installed during the **first start** into the OS-specific application data folder. The resource path is defined by the overridden `getConfigTemplateFile`. In a Maven/Gradle project the resource root is usually `src/main/resources`; the resources are packed into your JAR preserving the directory structure. |
| `installer-steps.cfg` | Defines the installer steps shown during installation. **If the file is missing, no installer is shown**; the same holds when no steps are defined. Loaded relative to your app class (e.g. `<resource-path>/ch/apps/cfg/installer-steps.cfg`). Property syntax; the key is a number (order, starting at 1), the value is a class implementing `ch.autumo.ui.fx.controller.actions.installer.Step`. |
| `edit.xml` | Theme configuration for the [RSyntaxTextArea](https://bobbylight.github.io/RSyntaxTextArea/) syntax-highlighting component (unused by the framework itself). Useful if you build a text editor in your application (see the autumo Documents Viewer). Loadable with RSyntaxTextArea's theme loader. |
| `theme.dtd` | The document type definition required for theme files such as `edit.xml`. |

**Application data locations** (after first start):

| OS | Location |
| --- | --- |
| Linux | `/home/<USER>/.<APPNAME>` (no manufacturer name used) |
| macOS | `/Users/<USER>/Library/Application Support/<MANUFACTURER>/<APPNAME>` |
| Windows | `C:\Users\<USER>\AppData\Roaming\<MANUFACTURER>\<APPNAME>` |

The manufacturer is read from the overridden `getManufacturer`.

**`app.cfg` keys** (Java-properties syntax):

| Key | Meaning |
| --- | --- |
| `url.website`, `url.purchase`, `url.documentation`, `url.website.updates` | URLs used to refer the customer to websites. |
| `url.website.version.file` | URL of the product version file used by the check-for-update mechanism, e.g. `https://products.autumo.ch/xml/template.xml`. |
| `email.sales`, `email.support` | Contact e-mail addresses for sales and support. |
| `disableMacOSMenuDetach` | Set to `yes` to disable the default detaching of the application menu to the macOS menu-bar. |
| `primaryStageStyle` | Decoration of the primary stage: `DECORATED`, `UNDECORATED`, `TRANSPARENT` or `UTILITY`. |
| `installer.licStepOverOS` | Tell the installer to skip the default license step per system: `mac`, `win`, `linux`. Useful when an external installer already showed a license before the in-app installer defined by `installer-steps.cfg`. |
| `https.protocols` | Protocols used for HTTP connections. |

All configuration parameters are available at runtime through your `BaseApp` subclass
and the autumo-commons `ConfigurationManager`; file manipulation is possible with the
`Helper` class.

**Default installer steps** (`ch.autumo.ui.fx.controller.actions.installer`):

- `StepAbout` — shows the resource `html/about_LANG.html` (relative to your app class)
  in a dialog, where `LANG` is the 2-character language code from the system settings
  (or `en` if unavailable).
- `StepLicense` — shows `html/license_LANG.html` in a dialog with a small box that must
  be clicked to accept the license; the acceptance is written to the configuration file.
- `StepDone` — a simple "Done" dialog.

> In every step (`BaseStep`) and every action (`BaseAction`), the controller is
> available, and through it the **body of the GUI is a JavaFX pane**.

### 4.2 `data`

| Resource | Meaning |
| --- | --- |
| `countries.csv` | Table of country names and the resource path to a country flag image in the autumo UI library. Used by the registration mask (opened via the `UI` class), which is only used together with the registration/licensing module. |

### 4.3 `view`

| Resource | Meaning |
| --- | --- |
| `defaultInfoPane.fxml`, `registrationPane.fxml` | The FXML files for the standard dialogs; they open in Scene Builder. To change them, use the `showPane` UI methods where you can specify the FXML for your dialog. The resource path is always loaded relative to the view class (a `Pane`/`BorderPane`), so to customize library masks you patch `ch.autumo.ui.fx.view.DefaultInfoView` (all dialogs) or `ch.autumo.ui.fx.view.RegistrationView`. |
| `main.fxml` (Demo) | The FXML of the UI Demo; use it as a template for your primary stage / main window if you want a toolbar and a basic layout — or build your own view from scratch. |

### 4.4 `lang`

All texts are gettext PO files; there are no `.properties` catalogues anymore.

| Resource | Meaning |
| --- | --- |
| `messages.pot` | Template of the autumo-ui framework texts: every message id, empty `msgstr`. Translator reference — English is the source language, so the message id is the English text (`{0}`, `{1}`, … are `java.text.MessageFormat` arguments). |
| `messages.en.po`, `messages.de.po` | The framework's own catalogues (English/German). |
| `messages.<LANG>.po` (pattern) | Add a language by replacing `<LANG>` with a code of `Constants.LANGUAGES`. |

An application ships the catalogue of its own texts as `<your base resource path>/lang/messages.<LANG>.po`. Both catalogues are **merged** by the framework (`Helper.getMessageBundle`): the application's file is looked up first, the autumo-ui file fills the rest, so an application translates only its own texts — and may still override any framework text by defining its key. See `Helper.getTranslatedText`, `Helper.getText` and `Helper.setLanguage`.

### 4.5 `style`

| Resource | Meaning |
| --- | --- |
| `application.css` | The stylesheet for the whole application. Loaded relative to your app class (e.g. `<resource-path>/ch/apps/style/application.css`). You are free to load additional stylesheets for controls and components. |
| `html.css` | Stylesheet for HTML views. |

The view constructors decide what a scene gets: `null` as style resource installs the
**library base style**, a path such as `Constants.STYLESHEET` installs **your** sheet instead
(it *replaces* the base look, so such a sheet has to be complete). The standard dialogs do
both — base style first, your `application.css` on top, if you ship one
(`Helper.getApplicationStyleSheets(app)`) — so a sheet that only carries overrides keeps the
framework look and its palette everywhere.

### 4.6 `icons`

Two icon mechanisms coexist:

- **PNGs** in **16×16 px** and **48×48 px** (`ch/autumo/ui/icons`) that can be used as
  resources. When opening a dialog with the `UI` `showPane` methods you simply name e.g.
  `about-48.png` (or a Phosphor name such as `info`, `x-circle`, `gear-six`, `trash`,
  `download-simple`, `seal-check`). The pane-header icons of the standard framework panes
  (error, config, about, register, remove-config, update) now use Phosphor glyphs; their
  PNGs remain only as fallbacks / for applications that reference them directly. Language
  flags (`lang_de.png`, `lang_en.png`) are still shipped for the language menu items;
  `language-16.png` and the `icons/flags/` directory are no longer shipped. The busy cursor
  is rendered from the Phosphor `circle-notch` glyph.
- **Phosphor icon font** (`fonts/Phosphor.ttf`, MIT, `fonts/Phosphor-LICENSE.txt`) with
  1530 icons, addressed by name via `ch.autumo.ui.fx.control.Icons`
  (`Icons.create(name)`, `Icons.create(name, size)`, `Icons.isAvailable(name)` →
  `javafx.scene.text.Text`, or `null`). The name/codepoint table is
  `fonts/phosphor-codepoints.properties`. Use this for toolbar/menu/dialog glyphs so they
  scale crisply and follow the theme colours (base `.ph-icon`, plus `ph-icon-info`,
  `ph-icon-warning`, `ph-icon-error`). The standard Alert dialogs keep JavaFX's own
  graphics and are **not** overridden.

- **Application icon** — every autumo-ui application has a window/task-bar icon on all
  operating systems without further effort. `BaseApp.start()` applies the icons returned by
  `Helper.getApplicationIcons(app)` (sizes 16, 32, 48, 64, 128, 256, ascending):
  1. your own icons — `icons/app-16.png` … `icons/app-256.png` (any subset), or a single
     `icons/app.png` (at least 256×256 px), resolved relative to your app class;
  2. otherwise the library default — `icons/autumo-ui-logo-16.png` …
     `icons/autumo-ui-logo-256.png`, with the vector source `icons/autumo-ui-logo.svg`
     (dark grey rounded button, "UI" lettering, red "FX" corner badge).
  The macOS dock icon is **not** covered by stage icons (JDK-8095033); it is defined by the
  packaged application: jpackage `--icon` with your `icons/app.icns`, or the library
  default `icons/autumo-ui-logo.icns` / `icons/autumo-ui-logo.ico` for Windows.

The library also ships the **Inter** font family and a default stylesheet, which are
used as the framework default look.

### 4.7 `html`

HTML bodies of the standard panes, loaded relative to your app class.

| Resource | Meaning |
| --- | --- |
| `html/about_LANG.html`, `html/license_LANG.html` | Body of the About/License pane and of the installer's about/license steps. These are **application** resources — an application ships its own texts (`LANG` = 2-character language code, `en` as fallback). |
| `html/remove_LANG.html` | Body of the **Remove app-data** pane (`UI.showRemovePane`). The library ships a generic default (`ch/autumo/ui/html/remove_en.html`, `remove_de.html`), so the pane works out of the box; ship the file in your application only if you want different wording. The lookup `Helper.getAppResourceOrBase(app, path)` implements exactly this: your resource first, the library default second — use it for any pane content an application may, but does not have to, provide. |

## 5. Building and packaging

- **Build the module:**
  ```sh
  ./gradlew build                # compile + jar
  ./gradlew publishToMavenLocal  # installs ch.autumo.commons:autumo-ui-fx:1.2.0 to ~/.m2
  ```
- **Dependencies of the module** (see `build.gradle`): the JavaFX
  25 modules (`javafx-controls`, `javafx-fxml`, `javafx-web`, `javafx-swing`),
  `rsyntaxtextarea:3.5.0`, `validatorfx:1.0.0`, `gson:2.11.0`, Log4j2 `2.26.1`
  and `commons-lang3:3.21.0`.
- **Install the library into the local Maven repository** (for Maven/Gradle consumers):
  ```sh
  ./gradlew publishToMavenLocal  # installs ch.autumo.commons:autumo-ui-fx:1.2.0 to ~/.m2
  ```
  Take a look at the `build.gradle` build script within the UI Demo project to see how
  to set up your own development project, including the 3rd-party dependencies.

**Order of steps for a first run:**

1. `./gradlew publishToMavenLocal` in this project — publishes `autumo-ui-fx:1.2.0`.
2. `./gradlew run` in the UI Demo project — starts the example application.

autumo-ui itself has no `run` task (it is a library). The Demo is the reference
application and the quickest way to see the framework running.

## 6. Consuming the module

### 6.1 Dependencies of the module

| Dependency                                | Scope      | Note |
| ----------------------------------------- | ---------- | ----- |
| `org.openjfx:javafx-controls/-fxml/-web/-swing:25` | `implementation` | The JavaFX modules. `javafx-swing` is required for the code editor (`SwingNode`). |
| `com.fifesoft:rsyntaxtextarea:3.5.0`      | `api`      | The code editor component (RSyntaxTextArea + RTextScrollPane, BSD-2-Clause), embedded into JavaFX with a SwingNode. |
| `net.synedra:validatorfx:1.0.0`, `com.google.code.gson:gson:2.11.0`, `org.apache.logging.log4j:*:2.26.1`, `org.apache.commons:commons-lang3:3.21.0` | `implementation` | Validation, the logging and the utilities. |

### 6.2 Adding it to a project

**Maven** (after `./gradlew publishToMavenLocal`):

```xml
<dependency>
    <groupId>ch.autumo.commons</groupId>
    <artifactId>autumo-ui-fx</artifactId>
    <version>1.2.0</version>
</dependency>
```

**Gradle:**

```groovy
implementation "ch.autumo.commons:autumo-ui-fx:1.2.0"
```

> **JavaFX does not come transitively** — declare it in your own project (see 2.1).
> If you pin your own JavaFX version, use it consistently for the plugin and the dependencies.

## 7. Further questions?

Visit our Discord server: <https://discord.gg/xvAJrv6wmb>.

## 8. Third-Party Licenses

See `THIRDPARTYLICENSES.md`.

## 9. License

Apache License — Version 2.0, January 2004.

<br />

---

<p align="center">Copyright &copy; 2026 autumo GmbH. All rights reserved.</p>
