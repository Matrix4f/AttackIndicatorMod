package me.stonepiano.cooldownfix.installer;

public class Command {

    private String name, desc;

    public Command(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
