/*
 * Copyright (c) 2002-2012, Mairie de Paris
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
package fr.paris.lutece.plugins.workflow.modules.archive.service;

import fr.paris.lutece.plugins.archive.util.ZipGenerateUtil;
import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.DirectoryHome;
import fr.paris.lutece.plugins.directory.business.EntryFilter;
import fr.paris.lutece.plugins.directory.business.EntryHome;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordHome;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.ConfigProducerService;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.utils.PDFUtils;
import fr.paris.lutece.plugins.workflow.modules.archive.business.TaskArchiveConfig;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.task.Task;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.signrequest.AbstractAuthenticator;
import fr.paris.lutece.util.string.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;


public class TaskArchive extends Task
{
	// PROPERTIES
	private static final String BEAN_REQUESTAUTHENTICATOR_FOR_URL = "workflow-archive.requestAuthenticatorForUrl";
	private static final String PROPERTY_TASK_ARCHIVE_TITLE = "module.workflow.archive.task_archive_title";
	private static final String PROPERTY_PDF_FOLDER_PATH = "module.workflow.archive.pdf_folder_path";
	private static final String PROPERTY_ZIP_FOLDER_PATH = "module.workflow.archive.zip_folder_path";

	// CONSTANTS
	public static final String EXTENSION_FILE_ZIP = ".zip";
	public static final String EXTENSION_FILE_PDF = ".pdf";
	public static final String CONSTANT_SLASH = "/";
	public static final String CONSTANT_UNDERSCORE = "_";

	@Autowired
	private IResourceHistoryService _resourceHistoryService;
	@Autowired
	private TaskArchiveConfigService _taskArchiveConfigService;
	@Autowired
	ConfigProducerService _configProducerService;
	private AbstractAuthenticator _authenticator = ( AbstractAuthenticator ) SpringContextService.getBean( BEAN_REQUESTAUTHENTICATOR_FOR_URL );

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRemoveConfig( )
	{
		_taskArchiveConfigService.remove( this.getId( ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRemoveTaskInformation( int nIdHistory )
	{
		// TODO Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTaskFormEntries( Locale locale )
	{
		// TODO Auto-generated method stub
		Map<String, String> res = new HashMap<String, String>( );
		res.put( "test", "lapin" );
		return res;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle( Locale locale )
	{
		return I18nService.getLocalizedString( PROPERTY_TASK_ARCHIVE_TITLE, locale );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init( )
	{
		// TODO Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processTask( int nIdResourceHistory, HttpServletRequest request, Locale locale )
	{
		Plugin plugin = PluginService.getPlugin( WorkflowArchivePlugin.PLUGIN_NAME );
		TaskArchiveConfig config = _taskArchiveConfigService.findByPrimaryKey( this.getId( ) );
		ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResourceHistory );

		if ( ( config != null ) && ( resourceHistory != null ) && resourceHistory.getResourceType( ).equals( Record.WORKFLOW_RESOURCE_TYPE ) )
		{
			String strZipURL = StringUtils.EMPTY;
			// _authenticator.buildSignature( arg0, arg1 );
			// We set the url in the record
			Record record = RecordHome.findByPrimaryKey( resourceHistory.getIdResource( ), plugin );

			IEntry entry = null;
			EntryFilter entryFilter = new EntryFilter( );
			entryFilter.setPosition( config.getIdEntryDirectory( ) );
			entryFilter.setIdDirectory( record.getDirectory( ).getIdDirectory( ) );

			List<IEntry> entryList = EntryHome.getEntryList( entryFilter, plugin );

			if ( ( entryList != null ) && !entryList.isEmpty( ) )
			{
				entry = EntryHome.findByPrimaryKey( entryList.get( 0 ).getIdEntry( ), plugin );
				// List<RecordField> listRecordFieldResult = new ArrayList<RecordField>( );

				// TODO : setter la valeur avec l'URL de telechargement
				// entry.getRecordFieldData( record, strZipURL, true, Boolean.FALSE, listRecordFieldResult, locale );
			}

			// We generate the PDF
			Directory directory = DirectoryHome.findByPrimaryKey( config.getIdDirectory( ), plugin );
			String strDirectoryName = StringUtil.replaceAccent( directory.getTitle( ) );
			String strPDFFolderPath = AppPathService.getAbsolutePathFromRelativePath( AppPropertiesService.getProperty( PROPERTY_PDF_FOLDER_PATH ) + CONSTANT_SLASH + strDirectoryName
					+ CONSTANT_UNDERSCORE + config.getIdEntryDirectory( ) );
			String strPDFFileName = strDirectoryName + CONSTANT_UNDERSCORE + config.getIdEntryDirectory( ) + EXTENSION_FILE_PDF;
			List<Integer> listIdEntryConfig = _configProducerService.loadListConfigEntry( plugin, config.getIdPDFProducerConfig( ) );

			File pdfFolder = new File( strPDFFolderPath );
			if ( !pdfFolder.exists( ) )
			{
				pdfFolder.mkdirs( );
			}
			File pdfFile = new File( strPDFFolderPath + CONSTANT_SLASH + strPDFFileName );

			// PDFUtils.doCreateDocumentPDF( request, strPDFFolderPath, out, listIdEntryConfig );
			OutputStream os = null;
			try
			{
				os = new FileOutputStream( pdfFile );
				PDFUtils.doCreateDocumentPDF( request, strPDFFileName, os, resourceHistory.getIdResource( ), listIdEntryConfig );
			}
			catch ( FileNotFoundException e )
			{
				AppLogService.error( e );
			}
			finally
			{
				IOUtils.closeQuietly( os );
			}

			// We generate the ZIP
			String strZipFolderPath = AppPathService.getAbsolutePathFromRelativePath( AppPropertiesService.getProperty( PROPERTY_ZIP_FOLDER_PATH ) );
			String strZIPFileName = strDirectoryName + CONSTANT_UNDERSCORE + config.getIdEntryDirectory( ) + EXTENSION_FILE_ZIP;
			try
			{
				ZipGenerateUtil.archiveDirectory( strPDFFolderPath, strZipFolderPath, strZIPFileName );
			}
			catch ( FileNotFoundException e )
			{
				AppLogService.error( e.getMessage( ), e );

			}
			catch ( IOException e )
			{
				AppLogService.error( e.getMessage( ), e );
			}
			pdfFile.delete( );

		}
	}
}
