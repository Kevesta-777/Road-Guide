package javax.lang.model;

/** Minimal Android stub for GraphHopper/Janino (javax.lang.model is not on Android). */
public enum SourceVersion {
    RELEASE_0, RELEASE_1, RELEASE_2, RELEASE_3, RELEASE_4, RELEASE_5, RELEASE_6, RELEASE_7,
    RELEASE_8, RELEASE_9, RELEASE_10, RELEASE_11, RELEASE_12, RELEASE_13, RELEASE_14,
    RELEASE_15, RELEASE_16, RELEASE_17, RELEASE_18, RELEASE_19, RELEASE_20, RELEASE_21;

    public static SourceVersion latest() { return RELEASE_17; }
    public static SourceVersion latestSupported() { return RELEASE_17; }

    public static boolean isIdentifier(CharSequence name) {
        if (name == null || name.length() == 0) return false;
        String s = name.toString();
        int cp = s.codePointAt(0);
        if (!Character.isJavaIdentifierStart(cp)) return false;
        for (int i = Character.charCount(cp); i < s.length(); ) {
            cp = s.codePointAt(i);
            if (!Character.isJavaIdentifierPart(cp)) return false;
            i += Character.charCount(cp);
        }
        return true;
    }

    public static boolean isName(CharSequence name) { return isName(name, latest()); }

    public static boolean isName(CharSequence name, SourceVersion version) {
        for (String id : name.toString().split("\\.", -1)) {
            if (!isIdentifier(id) || isKeyword(id, version)) return false;
        }
        return true;
    }

    public static boolean isKeyword(CharSequence name) { return isKeyword(name, latest()); }

    public static boolean isKeyword(CharSequence name, SourceVersion version) {
        switch (name.toString()) {
            case "abstract": case "assert": case "boolean": case "break": case "byte":
            case "case": case "catch": case "char": case "class": case "const": case "continue":
            case "default": case "do": case "double": case "else": case "enum": case "extends":
            case "final": case "finally": case "float": case "for": case "goto": case "if":
            case "implements": case "import": case "instanceof": case "int": case "interface":
            case "long": case "native": case "new": case "package": case "private":
            case "protected": case "public": case "return": case "short": case "static":
            case "strictfp": case "super": case "switch": case "synchronized": case "this":
            case "throw": case "throws": case "transient": case "try": case "void":
            case "volatile": case "while": case "true": case "false": case "null":
                return true;
            default:
                return false;
        }
    }
}
