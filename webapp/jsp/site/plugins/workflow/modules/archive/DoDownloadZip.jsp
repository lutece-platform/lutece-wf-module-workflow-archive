<jsp:useBean id="downloadArchive" scope="session" class="fr.paris.lutece.plugins.workflow.modules.archive.web.DownloadArchive" /><%
	downloadArchive.doDownloadFile(request,response);
%>