package com.cluster.snapshot;

import lombok.Data;

@Data
class ConfigMaps {
    String NAMESPACE, NAME, DATA, AGE;

    public ConfigMaps(String split[]) {
        int index = 0;
        for (String currentLine : split) {
            if (!currentLine.trim().equals("")) {
                switch (index) {
                    case 0:
                        this.setNAMESPACE(currentLine);
                        index++;
                        break;
                    case 1:
                        this.setNAME(currentLine);
                        index++;
                        break;
                    case 2:
                        this.setDATA(currentLine);
                        index++;
                        break;
                    case 3:
                        this.setAGE(currentLine);
                        index++;
                        break;
                }
            }
        }
    }
}
