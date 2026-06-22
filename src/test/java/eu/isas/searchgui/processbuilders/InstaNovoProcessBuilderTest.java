package eu.isas.searchgui.processbuilders;

import com.compomics.util.parameters.identification.tool_specific.InstaNovoParameters;
import java.io.File;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Tests the InstaNovo process builder command lines.
 *
 * @author CompOmics
 */
public class InstaNovoProcessBuilderTest extends TestCase {

    /**
     * Tests the three supported InstaNovo execution modes.
     *
     * @throws Exception if an exception occurs
     */
    public void testInstaNovoCommandLines() throws Exception {

        File instaNovoFolder = createInstaNovoFolder();
        File spectrumFile = createFile("input", ".mgf");
        File outputFile = createFile("output", ".csv");

        assertCommand(
                new InstaNovoProcessBuilder(instaNovoFolder, spectrumFile, outputFile, InstaNovoProcessBuilder.Mode.transformer, null, null).getCommand(),
                instaNovoFolder,
                spectrumFile,
                outputFile,
                "InstaNovo",
                "transformer",
                "predict",
                "--instanovo-model",
                InstaNovoParameters.DEFAULT_INSTANOVO_MODEL,
                null,
                null
        );

        assertCommand(
                new InstaNovoProcessBuilder(instaNovoFolder, spectrumFile, outputFile, InstaNovoProcessBuilder.Mode.diffusion, null, null).getCommand(),
                instaNovoFolder,
                spectrumFile,
                outputFile,
                "InstaNovo+",
                "diffusion",
                "predict",
                "--instanovo-plus-model",
                InstaNovoParameters.DEFAULT_INSTANOVO_PLUS_MODEL,
                "--no-refinement",
                null
        );

        assertCommand(
                new InstaNovoProcessBuilder(instaNovoFolder, spectrumFile, outputFile, InstaNovoProcessBuilder.Mode.refined, null, null).getCommand(),
                instaNovoFolder,
                spectrumFile,
                outputFile,
                "InstaNovo with InstaNovo+ refinement",
                "predict",
                null,
                "--instanovo-model",
                InstaNovoParameters.DEFAULT_INSTANOVO_MODEL,
                "--with-refinement",
                "--instanovo-plus-model"
        );
    }

    /**
     * Asserts a command line.
     *
     * @param command the command
     * @param instaNovoFolder the InstaNovo folder
     * @param spectrumFile the spectrum file
     * @param outputFile the output file
     * @param type the expected type
     * @param firstCommand the first command
     * @param secondCommand the second command
     * @param modelFlag the model flag
     * @param modelName the model name
     * @param modeFlag the mode flag
     * @param extraFlag the extra flag
     */
    private void assertCommand(
            List<String> command,
            File instaNovoFolder,
            File spectrumFile,
            File outputFile,
            String type,
            String firstCommand,
            String secondCommand,
            String modelFlag,
            String modelName,
            String modeFlag,
            String extraFlag
    ) {

        Assert.assertEquals(new File(instaNovoFolder, ".venv/bin/instanovo").getAbsolutePath(), command.get(0));
        Assert.assertTrue(command.contains(firstCommand));

        if (secondCommand != null) {
            Assert.assertTrue(command.contains(secondCommand));
        }

        Assert.assertTrue(command.contains("--data-path"));
        Assert.assertTrue(command.contains(spectrumFile.getAbsolutePath()));
        Assert.assertTrue(command.contains("--output-path"));
        Assert.assertTrue(command.contains(outputFile.getAbsolutePath()));
        Assert.assertTrue(command.contains("--denovo"));
        Assert.assertTrue(command.contains(modelFlag));
        Assert.assertTrue(command.contains(modelName));

        if (modeFlag != null) {
            Assert.assertTrue(command.contains(modeFlag));
        }

        if (extraFlag != null) {
            Assert.assertTrue(command.contains(extraFlag));
        }

        InstaNovoProcessBuilder.Mode mode;
        if (type.equals("InstaNovo+")) {
            mode = InstaNovoProcessBuilder.Mode.diffusion;
        } else if (type.equals("InstaNovo with InstaNovo+ refinement")) {
            mode = InstaNovoProcessBuilder.Mode.refined;
        } else {
            mode = InstaNovoProcessBuilder.Mode.transformer;
        }

        Assert.assertEquals(type, new InstaNovoProcessBuilder(instaNovoFolder, spectrumFile, outputFile, mode, null, null).getType());
    }

    /**
     * Creates a temporary InstaNovo folder with a virtual environment
     * executable.
     *
     * @return the InstaNovo folder
     *
     * @throws Exception if an exception occurs
     */
    private File createInstaNovoFolder() throws Exception {

        File folder = createFolder("instanovo-process");
        File executable = new File(folder, ".venv/bin/instanovo");
        executable.getParentFile().mkdirs();
        executable.createNewFile();
        executable.deleteOnExit();

        return folder;
    }

    /**
     * Creates a temporary file.
     *
     * @param prefix the prefix
     * @param suffix the suffix
     *
     * @return the file
     *
     * @throws Exception if an exception occurs
     */
    private File createFile(String prefix, String suffix) throws Exception {

        File file = File.createTempFile(prefix, suffix);
        file.deleteOnExit();

        return file;
    }

    /**
     * Creates a temporary folder.
     *
     * @param prefix the prefix
     *
     * @return the folder
     *
     * @throws Exception if an exception occurs
     */
    private File createFolder(String prefix) throws Exception {

        File folder = File.createTempFile(prefix, "");
        folder.delete();
        folder.mkdirs();
        folder.deleteOnExit();

        return folder;
    }
}
