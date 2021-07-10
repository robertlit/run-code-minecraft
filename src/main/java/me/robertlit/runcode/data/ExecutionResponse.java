package me.robertlit.runcode.data;

public class ExecutionResponse {

    public static final ExecutionResponse FAILED = new ExecutionResponse(null, null, null, null);

    private final String language;
    private final String version;
    private final Stage compile;
    private final Stage run;

    public ExecutionResponse(String language, String version, Stage compile, Stage run) {
        this.language = language;
        this.version = version;
        this.compile = compile;
        this.run = run;
    }

    public boolean success() {
        return language != null;
    }

    public String getLanguage() {
        return language;
    }

    public String getVersion() {
        return version;
    }

    public Stage getCompile() {
        return compile;
    }

    public Stage getRun() {
        return run;
    }

    public static class Stage {

        private final String output;

        public Stage(String output) {
            this.output = output;
        }

        public String getOutput() {
            return output;
        }
    }
}
