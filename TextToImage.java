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
            case "--list-families":
                showFamiles();
                return false;
            case "--list-font-names":
                showFontNames();
                return false;
            case "--list-font-names-in-family":
                family = args[++i];
                showFontNamesInFamily(family);
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
}
