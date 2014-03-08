package com.shpandrak.web.manage;

import com.shpandrak.datamodel.BaseEntity;
import com.shpandrak.xml.EntityXMLConverter;
import com.shpandrak.xml.EntityXMLConverterFactory;
import com.shpandrak.xml.XMLConverter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * Created with love
 * User: shpandrak
 * Date: 11/16/12
 * Time: 10:32
 */
public abstract class BaseWebmanageServlet<T extends BaseEntity> extends HttpServlet {

    protected abstract Class<T> getClassType();

    protected abstract XMLConverter<T> getXMLConverter();

    protected abstract String getRootResourcePath();

    protected abstract String getEntityClassLibraryName() throws IOException;

    protected abstract Set<String> getIncludeJSLibraries();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();

        out.println("<html>");
        out.println("<head>");
        out.println("\t<title>" + getClassType().getSimpleName() + " WebManager</title>");

        printCSS(out);
        out.println("\t<script src=\"prototype.js\" type=\"text/javascript\"></script>");
        out.println("\t<script src=\"jquery-1.8.3.js\" type=\"text/javascript\"></script>");
        for (String currLibrary : getIncludeJSLibraries()){
            out.println("\t<script src=\"" +currLibrary + "\" type=\"text/javascript\"></script>");
        }


        out.println("</head>");

        out.println("<body onLoad=\"getAll();\">");

        out.println("\t<script type=\"text/javascript\">");

        out.println("function log(what) {\n" +
                "    divLog.innerHTML = divLog.innerHTML + ((new\n" +
                "            Date()).toISOString()) + \": \" + what + \"<br />\";\n" +
                "}\n" +
                "\n" +
                "function clearLog() {\n" +
                "    divLog.innerHTML = \"\";\n" +
                "    log(\"log cleared\");\n" +
                "}\n");

        out.println(
        "function getAll() {\n" +
                getEntityClassLibraryName() + ".list(" +
                "function(xml) {\n" +
                "            this.txtAll.value = xml;\n" +
                "            " +  getEntityClassLibraryName() + ".fillTable(tabData, xml);\n" +
                "            log(\"fetch all successfuly\");\n" +
                "        },\n" +
                "        function(errString) {\n" +
                "            log(errString);\n" +
                "            alert(errString);\n" +
                "        }\n" +
                "    );\n" +
                "}\n");



                out.println("\t</script>");


        out.println("<br/>");
        out.println("<table width=\"100%\">");
        out.println("<tr><td>&nbsp;</td><td align=\"center\"><h3>" + getClassType().getSimpleName() + " WebManager</h3></td><td>&nbsp;</td>");
        out.println("<tr><td width=\"10%\">&nbsp;</td><td>");
                out.println("<table id=\"tabData\" class=\"CSSTableGenerator\">");
                out.println("</table>");
            out.println("</td><td width=\"10%\">&nbsp;</td></tr>");
        out.println("</table >");

        out.println("<br/><br/><br/>");


        out.println("Get All:<br/>");
        out.println("<textarea id=\"txtAll\"></textarea>");
        out.println("<input type=\"button\" value=\"getAll\" onClick=\"getAll();\" />");

        out.println("<div id=\"log-container\" style=\"clear:both;\">\n" +
                "    log output:\n" +
                "    <button onclick=\"clearLog();\">clear</button>\n" +
                "    <div id=\"divLog\"></div>\n" +
                "</div>\n");

        out.println("</body>");


        out.println("</html>");

    }

    private void printCSS(ServletOutputStream out) throws IOException {
        out.println("<style type=\"text/css\">\n" +
                "    body {\n" +
                "        width: 100%;\n" +
                "        height: 100%;\n" +
                "        border: 0px;\n" +
                "        margin: 0px;\n" +
                "        spacing: 0px;\n" +
                "        padding: 0px;\n" +
                "    }\n" +
                "\n" +
                "    div {\n" +
                "        border: 0px;\n" +
                "        margin: 0px;\n" +
                "        spacing: 0px;\n" +
                "        padding: 0px;\n" +
                "    }\n" +
                "\n" +
                "    textarea {\n" +
                "        width: 400px;\n" +
                "        height: 200px;\n" +
                "        border: 1px solid black;\n" +
                "        margin: 0px;\n" +
                "        spacing: 0px;\n" +
                "        padding: 0px;\n" +
                "    }\n" +
                ".CSSTableGenerator {\n" +
                "\n" +
                "\tmargin:0px;padding:0px;\n" +
                "\n" +
                "\twidth:100%;\n" +
                "\tbox-shadow: 10px 10px 5px #888888;\n" +
                "\n" +
                "\tborder:1px solid #000000;\n" +
                "\n" +
                "\t\n" +
                "\n" +
                "\t-moz-border-radius-bottomleft:0px;\n" +
                "\n" +
                "\t-webkit-border-bottom-left-radius:0px;\n" +
                "\n" +
                "\tborder-bottom-left-radius:0px;\n" +
                "\n" +
                "\t\n" +
                "\n" +
                "\t-moz-border-radius-bottomright:0px;\n" +
                "\n" +
                "\t-webkit-border-bottom-right-radius:0px;\n" +
                "\n" +
                "\tborder-bottom-right-radius:0px;\n" +
                "\n" +
                "\t\n" +
                "\n" +
                "\t-moz-border-radius-topright:0px;\n" +
                "\n" +
                "\t-webkit-border-top-right-radius:0px;\n" +
                "\n" +
                "\tborder-top-right-radius:0px;\n" +
                "\n" +
                "\t\n" +
                "\n" +
                "\t-moz-border-radius-topleft:0px;\n" +
                "\n" +
                "\t-webkit-border-top-left-radius:0px;\n" +
                "\n" +
                "\tborder-top-left-radius:0px;\n" +
                "\n" +
                "}.CSSTableGenerator table{\n" +
                "\n" +
                "\twidth:100%;\n" +
                "\n" +
                "\theight:100%;\n" +
                "\n" +
                "\tmargin:0px;padding:0px;\n" +
                "\n" +
                "}.CSSTableGenerator tr:last-child td:last-child {\n" +
                "\n" +
                "\t-moz-border-radius-bottomright:0px;\n" +
                "\n" +
                "\t-webkit-border-bottom-right-radius:0px;\n" +
                "\n" +
                "\tborder-bottom-right-radius:0px;\n" +
                "\n" +
                "}\n" +
                "\n" +
                ".CSSTableGenerator table tr:first-child td:first-child {\n" +
                "\n" +
                "\t-moz-border-radius-topleft:0px;\n" +
                "\n" +
                "\t-webkit-border-top-left-radius:0px;\n" +
                "\n" +
                "\tborder-top-left-radius:0px;\n" +
                "\n" +
                "}\n" +
                "\n" +
                ".CSSTableGenerator table tr:first-child td:last-child {\n" +
                "\n" +
                "\t-moz-border-radius-topright:0px;\n" +
                "\n" +
                "\t-webkit-border-top-right-radius:0px;\n" +
                "\n" +
                "\tborder-top-right-radius:0px;\n" +
                "\n" +
                "}.CSSTableGenerator tr:last-child td:first-child{\n" +
                "\n" +
                "\t-moz-border-radius-bottomleft:0px;\n" +
                "\n" +
                "\t-webkit-border-bottom-left-radius:0px;\n" +
                "\n" +
                "\tborder-bottom-left-radius:0px;\n" +
                "\n" +
                "}.CSSTableGenerator tr:hover td{\n" +
                "\n" +
                "\tbackground-color:#d3e9ff;\n" +
                "\n" +
                "\t\t\n" +
                "\n" +
                "\n" +
                "}\n" +
                "\n" +
                ".CSSTableGenerator td{\n" +
                "\n" +
                "\tvertical-align:middle;\n" +
                "\n" +
                "\t\tbackground:-o-linear-gradient(bottom, #ffffff 5%, #d3e9ff 100%);\tbackground:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #ffffff), color-stop(1, #d3e9ff) ); \n" +
                "\tbackground:-moz-linear-gradient( center top, #ffffff 5%, #d3e9ff 100% );\n" +
                "\tfilter:progid:DXImageTransform.Microsoft.gradient(startColorstr=\"#ffffff\", endColorstr=\"#d3e9ff\");\tbackground: -o-linear-gradient(top,#ffffff,d3e9ff);\n" +
                "\n" +
                "\n" +
                "\tbackground-color:#ffffff;\n" +
                "\n" +
                "\n" +
                "\tborder:1px solid #000000;\n" +
                "\n" +
                "\tborder-width:0px 1px 1px 0px;\n" +
                "\n" +
                "\ttext-align:left;\n" +
                "\n" +
                "\tpadding:7px;\n" +
                "\n" +
                "\tfont-size:10px;\n" +
                "\n" +
                "\tfont-family:Arial;\n" +
                "\n" +
                "\tfont-weight:normal;\n" +
                "\n" +
                "\tcolor:#000000;\n" +
                "\n" +
                "}.CSSTableGenerator tr:last-child td{\n" +
                "\n" +
                "\tborder-width:0px 1px 0px 0px;\n" +
                "\n" +
                "}.CSSTableGenerator tr td:last-child{\n" +
                "\n" +
                "\tborder-width:0px 0px 1px 0px;\n" +
                "\n" +
                "}.CSSTableGenerator tr:last-child td:last-child{\n" +
                "\n" +
                "\tborder-width:0px 0px 0px 0px;\n" +
                "\n" +
                "}\n" +
                "\n" +
                ".CSSTableGenerator tr:first-child td{\n" +
                "\n" +
                "\t\tbackground:-o-linear-gradient(bottom, #0057af 5%, #007fff 100%);\tbackground:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #0057af), color-stop(1, #007fff) );\n" +
                "\tbackground:-moz-linear-gradient( center top, #0057af 5%, #007fff 100% );\n" +
                "\tfilter:progid:DXImageTransform.Microsoft.gradient(startColorstr=\"#0057af\", endColorstr=\"#007fff\");\tbackground: -o-linear-gradient(top,#0057af,007fff);\n" +
                "\n" +
                "\n" +
                "\tbackground-color:#0057af;\n" +
                "\n" +
                "\tborder:0px solid #000000;\n" +
                "\n" +
                "\ttext-align:center;\n" +
                "\n" +
                "\tborder-width:0px 0px 1px 1px;\n" +
                "\n" +
                "\tfont-size:14px;\n" +
                "\n" +
                "\tfont-family:Arial;\n" +
                "\n" +
                "\tfont-weight:bold;\n" +
                "\n" +
                "\tcolor:#ffffff;\n" +
                "\n" +
                "}\n" +
                "\n" +
                ".CSSTableGenerator tr:first-child:hover td{\n" +
                "\n" +
                "\tbackground:-o-linear-gradient(bottom, #0057af 5%, #007fff 100%);\tbackground:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #0057af), color-stop(1, #007fff) );\n" +
                "\tbackground:-moz-linear-gradient( center top, #0057af 5%, #007fff 100% );\n" +
                "\tfilter:progid:DXImageTransform.Microsoft.gradient(startColorstr=\"#0057af\", endColorstr=\"#007fff\");\tbackground: -o-linear-gradient(top,#0057af,007fff);\n" +
                "\n" +
                "\n" +
                "\tbackground-color:#0057af;\n" +
                "\n" +
                "}\n" +
                "\n" +
                ".CSSTableGenerator tr:first-child td:first-child{\n" +
                "\n" +
                "\tborder-width:0px 0px 1px 0px;\n" +
                "\n" +
                "}\n" +
                "\n" +
                ".CSSTableGenerator tr:first-child td:last-child{\n" +
                "\n" +
                "\tborder-width:0px 0px 1px 1px;\n" +
                "\n" +
                "}" +
                "</style>\n");
    }


}
