package com.cluster.snapshot;

import java.util.Map;
import java.util.TreeMap;

class ClusterCommands {

    public static Map<String, String> clusterCommandsMap = new TreeMap<>();

    static {
        clusterCommandsMap.put("clusterName", "kubectl config current-context");
        clusterCommandsMap.put("helmChartsAllNamespaces", "helm ls --all");
        clusterCommandsMap.put("getAllPods", "kubectl get pods --all-namespaces -o wide");
        clusterCommandsMap.put("getAllPodsPlatform", "kubectl get pods -n platform");
        clusterCommandsMap.put("getAllPVC", "kubectl get persistentvolumes");
        clusterCommandsMap.put("getAllNodes", "kubectl get nodes");
        clusterCommandsMap.put("getClusterInfo", "kubectl cluster-info");
        clusterCommandsMap.put("getAllConfigMaps", "kubectl get configmaps --all-namespaces");
        clusterCommandsMap.put("getMemoryOfPods", "kubectl top pod --all-namespaces");
        clusterCommandsMap.put("getAllServices", "kubectl get svc --all-namespaces");
        clusterCommandsMap.put("getNodeMemoryDetails", "kubectl top nodes");
        clusterCommandsMap.put("getPersistentVolumes", "kubectl get pvc --all-namespaces");
        clusterCommandsMap.put("getAllDaemonSets", "kubectl get daemonset --all-namespaces");
        clusterCommandsMap.put("getStatefulSets", "kubectl get statefulsets --all-namespaces");
        clusterCommandsMap.put("getEverything", "kubectl get all --all-namespaces");
        clusterCommandsMap.put("getAllReplicaSets", "kubectl get replicasets --all-namespaces");
        clusterCommandsMap.put("getCurrentEvents", "kubectl get events --all-namespaces");
    }

    public static final String clusterName = "kubectl config current-context",
            helmChartsAllNamespaces = "helm ls --all",
            getAllPods = "kubectl get pods --all-namespaces -o wide",
            getAllPodsPlatform = "kubectl get pods -n platform",
            getAllPVC = "kubectl get persistentvolumes",
            getAllNodes = "kubectl get nodes",
            getClusterInfo = "kubectl cluster-info",
            getAllConfigMaps = "kubectl get configmaps --all-namespaces",
            getMemoryOfPods = "kubectl top pod --all-namespaces",
            getAllServices = "kubectl get svc --all-namespaces",
            getNodeMemoryDetails = "kubectl top nodes",
            getPersistentVolumes = "kubectl get pvc --all-namespaces";


    public static String getCommandToDescribeConfigMap(ConfigMaps configMap) {
        return "kubectl describe configmaps " + configMap.getNAME() + " -n " + configMap.getNAMESPACE();
    }

    public static String getGetPodLogsCommand(PodDetails pod) {
        return "kubectl logs -n " + pod.getNAMESPACE() + " " + pod.getPodName() + " --tail=1000";
    }

    public static String getGetDescribePodCommand(PodDetails pod) {
        return "kubectl describe po/" + pod.getPodName() + " -n " + pod.getNAMESPACE();
    }

    public static String getGetPodEnvironmentCommand(PodDetails pod) {
        return "kubectl exec " + pod.getPodName() + " env -n " + pod.getNAMESPACE();
    }

    public static String getCommandToDescribeService(Services svc) {
        return "kubectl describe svc " + svc.getNAME() + " -n " + svc.getNAMESPACE();
    }

    public static String getCommandToDescribeSecret(String secretname, String namespace) {
        return "kubectl describe secret " + secretname + " -n " + namespace;
    }
}
