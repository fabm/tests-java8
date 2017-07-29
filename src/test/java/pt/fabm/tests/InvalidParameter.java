package pt.fabm.tests;

public class InvalidParameter extends RuntimeException {
    private String name;

    public InvalidParameter() {
    }

    public InvalidParameter(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
