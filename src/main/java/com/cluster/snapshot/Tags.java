package com.cluster.snapshot;

public class Tags {
    public static final String
            Watch = "<i class='material-icons'>watch</i>",
            BalanceScale = "<i class='fa fa-balance-scale'></i>",
            RightArrow = "<i class='fa fa-arrow-right'></i>",
            MemoryNotFound = "<i class='fa fa-battery-0'></i>",
            LowMemoryUsage = "<i class='fa fa-battery-1' style='color: green'></i>",
            MediumMemoryUsage = "<i class='fa fa-battery-2' style='color: orange'></i>",
            HighMemoryUsage = "<i class='fa fa-battery-4' style='color: red'></i>",
            Restarts = "<i class='fa fa-undo'></i>",
            CrashLoopBackOff = "<i class='fa fa-refresh fa-spin fa-fw'></i>",
            MapIcon = "<i class='fa fa-map'></i>";

    public static String getHtmlHeadScript() {
        return "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\">\n" +
                " <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/icon?family=Material+Icons\">  <style type=\"text/css\">\n" +
                "    a          { text-decoration: none;  }\n" +
                "    div.on     { display:         block; }\n" +
                "    div.off    { display:         none;  }\n" +
                "  </style>\n" +
                "  <script type=\"text/javascript\">\n" +
                "    <!--\n" +
                "    function show( item_id ) {\n" +
                "      document.getElementById( 'id_' + item_id ).className = 'on';\n" +
                "    }\n" +
                "\n" +
                "    function hide( item_id ) {\n" +
                "      document.getElementById( 'id_' + item_id ).className = 'off';\n" +
                "    }\n" +
                "\n" +
                "    function toggle( item_id ) {\n" +
                "      if( document.getElementById( 'id_' + item_id ).className == 'on') {\n" +
                "        hide( item_id );\n" +
                "      }\n" +
                "      else {\n" +
                "        show( item_id );\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    function expand( max_id ) {\n" +
                "      for( id = 1; id < max_id; ++id ) {\n" +
                "        toggle( id );\n" +
                "      }\n" +
                "    }\n" +
                "    \n" +
                "    -->\n" +
                "  </script>\n" +
                "<body style='background-color: white; background: white'>";
    }

    public static String createIngressIpLink(ClusterDetails details) {
        return "<h3>" + Tags.BalanceScale + "Ingress IP : <a target='_blank' href='https://" +
                details.ingressIp + "' style='color:hotpink' >" + details.ingressIp + "</a></h3>";
    }
}
