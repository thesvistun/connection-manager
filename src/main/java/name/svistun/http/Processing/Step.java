package name.svistun.http.Processing;

import java.util.LinkedList;
import java.util.List;

public class Step {
    private List<String> args;
    private List<Object> replacement;
    private String type;

    public Step(String type, List<String> args) {
        this.args = args;
        this.type = type;
        replacement = new LinkedList<>();
        if (type.equals("replace_line")) {
            char[] replacementArr = args.get(1).toCharArray();
            boolean start = false;
            boolean slash = false;
            String str = "";
            for (char currentChar : replacementArr) {
                // "
                if (currentChar == '"') {
                    if (start) {
                        if (slash) {
                            str = str.concat(String.valueOf(currentChar));
                            slash = false;
                        } else {
                            replacement.add(str);
                            str = "";
                            start = false;
                        }
                    } else {
                        start = true;
                    }
                // \
                } else if (currentChar == '\\') {
                    slash = true;
                // char
                } else {
                    if (start) {
                        if (slash) {
                            str = str.concat(String.valueOf('\\'));
                            slash = false;
                        }
                        str = str.concat(String.valueOf(currentChar));
                    } else {
                        replacement.add(new MatchGroup(Integer.parseInt(String.valueOf(currentChar))));
                    }
                }
            }
        }
    }

    String getArg(int number) {
        return args.get(number - 1);
    }

    List<Object> getReplacement() {
        return replacement;
    }

    String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Step{" +
                "type='" + type + '\'' +
                '}';
    }

    class MatchGroup {
        private int number;

        MatchGroup(int number) {
            this.number = number;
        }

        int getNumber() {
            return number;
        }
    }
}
