package com.cluster.snapshot;

import lombok.Data;

@Data
class PodMemoryDetail {
    String NAMESPACE, NAME, CPU_cores, MEMORY_in_bytes;

    public PodMemoryDetail(String split[]) {
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
                        this.setCPU_cores(currentLine);
                        index++;
                        break;
                    case 3:
                        this.setMEMORY_in_bytes(currentLine);
                        index++;
                        break;
                }
            }
        }
    }
}
