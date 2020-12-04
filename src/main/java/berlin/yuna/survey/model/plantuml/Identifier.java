package berlin.yuna.survey.model.plantuml;

import net.sourceforge.plantuml.cucadiagram.Code;

public class Identifier implements Code {

    final String name;

    public Identifier(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Code eventuallyRemoveStartingAndEndingDoubleQuote(String s) {
        return this;
    }
}
