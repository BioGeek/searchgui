package eu.isas.searchgui.processbuilders;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.parameters.identification.tool_specific.InstaNovoParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Process builder for InstaNovo and InstaNovo+.
 *
 * @author CompOmics
 */
public class InstaNovoProcessBuilder extends SearchGUIProcessBuilder {

    /**
     * The InstaNovo executable name.
     */
    public static final String EXECUTABLE_FILE_NAME = "instanovo";

    /**
     * InstaNovo execution modes.
     */
    public enum Mode {
        transformer,
        diffusion,
        refined
    }

    /**
     * The input spectrum file.
     */
    private final File spectrumFile;
    /**
     * The output file.
     */
    private final File outputFile;
    /**
     * The mode.
     */
    private final Mode mode;

    /**
     * Constructor.
     *
     * @param instaNovoFolder the InstaNovo folder
     * @param spectrumFile the spectrum file
     * @param outputFile the output file
     * @param mode the mode
     * @param waitingHandler the waiting handler
     * @param exceptionHandler the exception handler
     */
    public InstaNovoProcessBuilder(
            File instaNovoFolder,
            File spectrumFile,
            File outputFile,
            Mode mode,
            WaitingHandler waitingHandler,
            ExceptionHandler exceptionHandler
    ) {

        this.spectrumFile = spectrumFile;
        this.outputFile = outputFile;
        this.mode = mode;
        this.waitingHandler = waitingHandler;
        this.exceptionHandler = exceptionHandler;

        File executableFile = getExecutable(instaNovoFolder);
        process_name_array.add(executableFile.getPath());

        if (mode == Mode.transformer) {
            process_name_array.add("transformer");
            process_name_array.add("predict");
        } else if (mode == Mode.diffusion) {
            process_name_array.add("diffusion");
            process_name_array.add("predict");
        } else {
            process_name_array.add("predict");
        }

        process_name_array.add("--data-path");
        process_name_array.add(spectrumFile.getAbsolutePath());
        process_name_array.add("--output-path");
        process_name_array.add(outputFile.getAbsolutePath());
        process_name_array.add("--denovo");

        if (mode == Mode.transformer || mode == Mode.refined) {
            process_name_array.add("--instanovo-model");
            process_name_array.add(InstaNovoParameters.DEFAULT_INSTANOVO_MODEL);
        }

        if (mode == Mode.diffusion || mode == Mode.refined) {
            process_name_array.add("--instanovo-plus-model");
            process_name_array.add(InstaNovoParameters.DEFAULT_INSTANOVO_PLUS_MODEL);
        }

        if (mode == Mode.diffusion) {
            process_name_array.add("--no-refinement");
        } else if (mode == Mode.refined) {
            process_name_array.add("--with-refinement");
        }

        process_name_array.trimToSize();

        System.out.println(System.getProperty("line.separator") + System.getProperty("line.separator") + "instanovo command: ");
        for (Object element : process_name_array) {
            System.out.print(element + " ");
        }
        System.out.println(System.getProperty("line.separator"));

        pb = new ProcessBuilder(process_name_array);
        if (instaNovoFolder != null && instaNovoFolder.exists()) {
            pb.directory(instaNovoFolder);
        }
        pb.redirectErrorStream(true);
    }

    /**
     * Returns the executable.
     *
     * @param instaNovoFolder the InstaNovo folder
     *
     * @return the executable
     */
    private File getExecutable(File instaNovoFolder) {

        if (instaNovoFolder != null) {

            File virtualEnvironmentExecutable = new File(
                    instaNovoFolder,
                    ".venv" + File.separator + "bin" + File.separator + EXECUTABLE_FILE_NAME
            );

            if (virtualEnvironmentExecutable.exists()) {
                virtualEnvironmentExecutable.setExecutable(true);
                return virtualEnvironmentExecutable;
            }

            File executable = new File(instaNovoFolder, EXECUTABLE_FILE_NAME);

            if (executable.exists()) {
                executable.setExecutable(true);
                return executable;
            }
        }

        return new File(EXECUTABLE_FILE_NAME);
    }

    /**
     * Returns the command line used by the process builder.
     *
     * @return the command line used by the process builder
     */
    public List<String> getCommand() {

        ArrayList<String> result = new ArrayList<>();

        for (Object argument : process_name_array) {
            result.add(argument.toString());
        }

        return result;
    }

    @Override
    public String getType() {
        if (mode == Mode.diffusion) {
            return "InstaNovo+";
        } else if (mode == Mode.refined) {
            return "InstaNovo with InstaNovo+ refinement";
        }
        return "InstaNovo";
    }

    @Override
    public String getCurrentlyProcessedFileName() {
        return spectrumFile.getName();
    }
}
