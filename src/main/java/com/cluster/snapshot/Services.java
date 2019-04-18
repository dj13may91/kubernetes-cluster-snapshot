package com.cluster.snapshot;

import lombok.Data;

@Data
class Services {
    String NAMESPACE, NAME, TYPE, CLUSTER_IP, EXTERNAL_IP, PORTs, AGE;

    public Services(String split[]) {
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
                        this.setTYPE(currentLine);
                        index++;
                        break;
                    case 3:
                        this.setCLUSTER_IP(currentLine);
                        index++;
                        break;
                    case 4:
                        this.setEXTERNAL_IP(currentLine);
                        index++;
                        break;
                    case 5:
                        this.setPORTs(currentLine);
                        index++;
                        break;
                    case 6:
                        this.setAGE(currentLine);
                        index++;
                        break;
                }
            }
        }
    }
}
