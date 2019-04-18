package com.cluster.snapshot;

import lombok.Data;

@Data
class PodDetails {
    String NAMESPACE, podName;
    String READY;
    String STATUS;
    int RESTARTS;
    String AGE, IP, NODE;
    String PODMEMORY;
    String COLOUR = "blue";
    String LOGS;
    String ICON = " ";

    public PodDetails(String split[]) {
        int index = 0;
        for (String currentLine : split) {
            if (!currentLine.trim().equals("")) {
                switch (index) {
                    case 0:
                        this.setNAMESPACE(currentLine);
                        index++;
                        break;
                    case 1:
                        this.setPodName(currentLine);
                        index++;
                        break;
                    case 2:
                        this.setREADY(currentLine);
                        index++;
                        break;
                    case 3:
                        this.setSTATUS(currentLine);
                        index++;
                        break;
                    case 4:
                        try {
                            this.setRESTARTS(Integer.parseInt(currentLine));
                        }
                        catch(Exception e){
                            this.setRESTARTS(-1);
                    }
                        index++;
                        break;
                    case 5:
                        this.setAGE(currentLine);
                        index++;
                        break;
                    case 6:
                        this.setIP(currentLine);
                        index++;
                        break;
                    case 7:
                        this.setNODE(currentLine);
                        index++;
                        break;
                }
            }
        }
    }

    public void UpdatePod(String memory_in_bytes) {
        setPODMEMORY(memory_in_bytes);
        if (getRESTARTS() != 0) {
            setICON(Tags.Restarts);
            setPODMEMORY(getPODMEMORY() + "<br><b>Number of restarts: " + getRESTARTS() + " </b>");
        }
        long memoryUsage;
        try {
            memoryUsage = Long.parseLong(getPODMEMORY().split("Mi")[0]);
            if (memoryUsage > 1000) {
                setICON(getICON() + Tags.HighMemoryUsage);
            } else if (memoryUsage > 500) {
                setICON(getICON() + Tags.MediumMemoryUsage);
            } else {
                setICON(getICON() + Tags.LowMemoryUsage);
            }
        } catch (Exception e) {
            System.out.println("Update Pod: " + podName.toString() + ". Exception" + e.getMessage());
            setICON(getICON() + Tags.MemoryNotFound);
        }
        if (!getSTATUS().equalsIgnoreCase("Running")) {
            setPODMEMORY(getPODMEMORY() + "<br>Reason of restarts: " + getSTATUS());
            setICON(getICON() + Tags.CrashLoopBackOff);
        }
    }
}
