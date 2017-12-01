import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public final class TextToImage extends Application {
    private static String text = "Hello, world!";

    // Font Related
    private static boolean isNameUsed = false;
    private static String name = "System Regular";
    private static String family = "System";
    private static FontWeight weight = FontWeight.NORMAL;
    private static FontPosture posture = FontPosture.REGULAR;
    private static double size = 13;
    private static Font font = null;

    // Other Style Related
    private static Color foregroundColor = Color.BLACK;
    private static Color backgroundColor = Color.WHITE;
    private static boolean isUnderline = false;

    private static Label label = null;

    private static boolean isShown = false;
    private static String output = "out.png";
    private static String formatName = "png";

    public static final void main(final String... args) {
        if (!parseArgs(args)) {
            Platform.exit(); // "return" alone is not enough to quit
            return;
        }

        // Setup Font
        if (isNameUsed) {
            font = new Font(name, size);
        } else {
            font = Font.font(family, weight, posture, size);
        }
        System.out.printf("Font Used: %s\n", font.getName());

        // Setup Label
        label = setupLabel(font, text, isUnderline);

        if (isShown) {
            launch(args);
        } else {
            // If saveSceneToFile is executed on the main thread, it throws:
            // java.lang.IllegalStateException: Not on FX application thread; currentThread = main
            final CountDownLatch latch = new CountDownLatch(1);
            final AdvancedRunnable runnable = new AdvancedRunnable(
                () -> saveSceneToFile(new Scene(label), formatName, new File(output)),
                () -> latch.countDown()
            );
            Platform.runLater(runnable);
            try {
                latch.await();
                final Exception exception = runnable.getException();
                if (exception != null) {
                    throw new RuntimeException(exception);
                }
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
            Platform.exit();
        }
    }

    private static final class AdvancedRunnable implements Runnable {
        private final Runnable runnable;
        private final Runnable finallyRunnable;
        private Exception exception;

        public AdvancedRunnable(final Runnable runnable, final Runnable finallyRunnable) {
            this.runnable = runnable;
            this.finallyRunnable = finallyRunnable;
        }

        @Override
        public final void run() {
            try {
                runnable.run();
            } catch (final Exception e) {
                exception = e;
            } finally {
                finallyRunnable.run();
            }
        }

        public final Exception getException() {
            return exception;
        }
    }

    private static final Label setupLabel(final Font font, final String text, final boolean isUnderline) {
        final Label label = new Label();
        label.setAlignment(Pos.CENTER);
        label.setContentDisplay(ContentDisplay.TEXT_ONLY);
        label.setEllipsisString(null);
        label.setFont(font);
        label.setGraphic(null);
        label.setGraphicTextGap(0);
        label.setLineSpacing(0);
        label.setMnemonicParsing(false);
        label.setText(text);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setTextFill(foregroundColor);
        label.setTextOverrun(OverrunStyle.CLIP);
        label.setUnderline(isUnderline);
        label.setWrapText(false);
        label.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
        return label;
    }

    // NOTE: MUST be called on the Java FX thread.
    private static final void saveSceneToFile(
        final Scene scene,
        final String formatName,
        final File file
    ) {
        final WritableImage writableImage = scene.snapshot(null);
        final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
        try {
            ImageIO.write(bufferedImage, formatName, file);
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void start(final Stage primaryStage) {
        final Scene scene = new Scene(label);

        primaryStage.setTitle("Text to Image");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();

        saveSceneToFile(scene, formatName, new File(output));
    }

    private static final boolean parseArgs(final String... args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "-t":
            case "--text":
                text = args[++i];
                break;
            case "-n":
            case "--name":
                name = args[++i];
                isNameUsed = true;
                break;
            case "-f":
            case "--family":
                family = args[++i];
                isNameUsed = false;
                break;
            case "-w":
            case "--weight":
                weight = FontWeight.valueOf(args[++i].toUpperCase(Locale.ROOT));
                break;
            case "--bold":
                weight = FontWeight.BOLD;
                break;
            case "-p":
            case "--posture":
                posture = FontPosture.valueOf(args[++i].toUpperCase(Locale.ROOT));
                break;
            case "--italic":
                posture = FontPosture.ITALIC;
                break;
            case "-s":
            case "--size":
                size = Double.valueOf(args[++i]);
                break;
            case "--fgColor":
                foregroundColor = Color.valueOf(args[++i]);
                break;
            case "--bgColor":
                backgroundColor = Color.valueOf(args[++i]);
                break;
            case "--underline":
                isUnderline = true;
                break;
            case "-o":
            case "--output":
                output = args[++i];
                int index;
                if ((index = output.lastIndexOf('.')) == -1) {
                    throw new RuntimeException("Unknown output file format");
                }
                formatName = output.substring(index + 1);
                break;
            case "--show":
                isShown = true;
                break;
            case "--families":
                showFamiles();
                return false;
            case "--names":
                showFontNames();
                return false;
            case "--names-in":
                family = args[++i];
                showFontNamesInFamily(family);
                return false;
            case "--help":
                showHelp();
                return false;
            default:
                text = args[i];
            }
        }
        return true;
    }

    private static final void showFamiles() {
        for (final String family : Font.getFamilies()) {
            System.out.println(family);
        }
    }

    private static final void showFontNames() {
        for (final String fontName : Font.getFontNames()) {
            System.out.println(fontName);
        }
    }

    private static final void showFontNamesInFamily(final String family) {
        for (final String fontName : Font.getFontNames(family)) {
            System.out.println(fontName);
        }
    }

    private static final String helpText = String.join(System.getProperty("line.separator"),
"Usage:",
"    java TextToImage [options...] <text>",
"",
"Options:",
"-t, --text TEXT        Specifies the text to be converted. This has the same",
"                       effect as specifying the text at the end of the command.",
"-n, --name NAME        Specifies the name of the font.",
"                       The default is 'System Regular'.",
"                       If this is enabled, -f, -w, -p will be disabled.",
"-f, --family FAMILY    Specifies the font family.",
"                       The default is 'System'.",
"                       If this is enabled, -n will be disabled.",
"-w, --weight WEIGHT    Specifies the font weight.",
"                       Available WEIGHTs are:",
"                       - THIN (100)",
"                       - EXTRA_LIGHT (200)",
"                       - LIGHT (300)",
"                       - NORMAL (400)",
"                       - MEDIUM (500)",
"                       - SEMI_BOLD (600)",
"                       - BOLD (700)",
"                       - EXTRA_BOLD (800)",
"                       - BLACK (900)",
"                       The default is 'NORMAL'.",
"                       If -n is enabled, this will be ignored.",
"--bold                 A shorthand for '-w BOLD'.",
"-p, --posture POSTURE  Specifies the font posture.",
"                       Available POSTUREs are:",
"                       - REGULAR",
"                       - ITALIC",
"                       The default is 'REGULAR'.",
"                       If -n is enabled, this will be ignored.",
"--italic               A shorthand for '-p ITALIC'.",
"-s, --size SIZE        Specifies the font size.",
"--fgColor COLOR        Specifies the foreground (text) color.",
"                       See below for available options.",
"--bgColor COLOR        Specifies the background color.",
"                       See below for available options.",
"--underline            Specifies whether the text should be underlined.",
"-o, --output OUTPUT    Specifies the output file.",
"--show                 Specifies whether the image should be shown on screen,",
"                       in addition to being generated.",
"--families             Print a list of available font families and quit.",
"--names                Print a list of available font names and quit.",
"--names-in FAMILY      Print a list of available font names",
"                       that belongs to the specified font family and quit.",
"--help                 Print help text.",
"",
"Colors:",
"Colors could be specified by:",
"- Common name like 'BLACK', and 'WHITE'",
"- Hexadecimal values like '0x336699', '0x369', '336699', and '369'",
"- Web RGB values like '#336699', '#369', and 'rgb(51,102,153)'",
"- Web RGBA values like 'rgba(51,102,153,1.0)'",
"- Web HSL values like 'hsl(270,100%,100%)'",
"- Web HSLA values like 'hsla(210,50%,40%,1.0)'",
"",
"For a full description, please visit:",
"https://docs.oracle.com/javase/9/docs/api/javafx/scene/paint/Color.html",
""
    );

    private static final void showHelp() {
        System.out.println(helpText);
    }
}
