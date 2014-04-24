/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.workflow.modules.archive.web;

import fr.paris.lutece.plugins.archive.util.ZipGenerateUtil;
import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.DirectoryHome;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.business.producerconfig.ConfigProducerHome;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.business.producerconfig.IConfigProducer;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.utils.PDFUtils;
import fr.paris.lutece.plugins.workflow.modules.archive.service.WorkflowArchivePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.signrequest.AbstractAuthenticator;
import fr.paris.lutece.util.string.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * Class to download zip archive files.
 * 
 */
public class DownloadArchive
{

	// CONSTANTS
	public static final String EXTENSION_FILE_ZIP = ".zip";
	public static final String CONSTANT_SLASH = "/";
	public static final String CONSTANT_UNDERSCORE = "_";

	// PROPERTIES
	private static final String PROPERTY_ZIP_FOLDER_PATH = "module.workflow.archive.zip_folder_path";
	private static final String BEAN_REQUEST_AUTHENTICATOR_FOR_URL = "workflow-archive.requestAuthenticatorForUrl";

	// PARAMETERS
	private static final String PARAM_ID_RECORD = "id_record";
	private static final String PARAM_ID_DIRECTORY = "id_directory";
	private static final String PARAM_ID_CONFIG_PRODUCER = "id_config_producer";

	private AbstractAuthenticator _authenticator = SpringContextService.getBean( BEAN_REQUEST_AUTHENTICATOR_FOR_URL );

	/**
	 * Initialize a download of a zip file if the user is authorized. The autorization is based on the signature given in parameter of the request.
	 * @param request The request
	 * @param response the response
	 */
	public void doDownloadFile( HttpServletRequest request, HttpServletResponse response )
	{
		String strIdRecord = request.getParameter( PARAM_ID_RECORD );
		String strIdDirectory = request.getParameter( PARAM_ID_DIRECTORY );
		String strIdConfigProducer = request.getParameter( PARAM_ID_CONFIG_PRODUCER );
		Plugin plugin = PluginService.getPlugin( WorkflowArchivePlugin.PLUGIN_NAME );

		if ( _authenticator.isRequestAuthenticated( request ) && StringUtils.isNotBlank( strIdRecord ) )
		{
			Directory directory = DirectoryHome.findByPrimaryKey( Integer.parseInt( strIdDirectory ), plugin );

			String strZipFolderPath = AppPathService.getAbsolutePathFromRelativePath( AppPropertiesService.getProperty( PROPERTY_ZIP_FOLDER_PATH ) );
			String strZIPFileName = strIdDirectory + CONSTANT_UNDERSCORE + strIdRecord + EXTENSION_FILE_ZIP;

			String strDownloadFileName = null;
			IConfigProducer configProducer = ConfigProducerHome.loadConfig( plugin, Integer.parseInt( strIdConfigProducer ) );
			if ( configProducer != null )
			{
				strDownloadFileName = PDFUtils.getFileNameFromConfig( directory, configProducer, Integer.parseInt( strIdRecord ), request.getLocale( ) );
			}
			else
			{
				strDownloadFileName = StringUtil.replaceAccent( directory.getTitle( ) ) + CONSTANT_UNDERSCORE + strIdRecord + EXTENSION_FILE_ZIP;
			}

			response.setHeader( "Content-Disposition", "attachment ;filename=\"" + strDownloadFileName + "\"" );
			response.setHeader( "Pragma", "public" );
			response.setHeader( "Expires", "0" );
			response.setHeader( "Cache-Control", "must-revalidate,post-check=0,pre-check=0" );

			response.setContentType( ZipGenerateUtil.ARCHIVE_MIME_TYPE_ZIP );
			try
			{
				OutputStream os = response.getOutputStream( );

				File zipFile = new File( strZipFolderPath + CONSTANT_SLASH + strZIPFileName );
				byte[] buffer = new byte[( int ) zipFile.length( )];
				FileInputStream is = new FileInputStream( zipFile );
				is.read( buffer );
				is.close( );
				os.write( buffer );
				os.flush( );
				os.close( );
			}
			catch ( IOException e )
			{
				AppLogService.error( e.getMessage( ), e );
			}
		}
	}
}