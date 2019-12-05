package cloudstack;

import lombok.Getter;

public class VM {
    @Getter
    private final String id;
    @Getter
    private final boolean isSystemVM;

    public VM(String id, String tagName){
        this.id = id;
        this.isSystemVM = tagName.equals("systemvm");
    }
}
