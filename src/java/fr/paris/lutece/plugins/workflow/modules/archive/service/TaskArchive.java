/*
 * Copyright (c) 2002-2013, Mairie de Paris
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
import fr.paris.lutece.plugins.directory.business.IndexerAction;
import fr.paris.lutece.plugins.directory.business.PhysicalFile;
import fr.paris.lutece.plugins.directory.business.PhysicalFileHome;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.business.RecordFieldFilter;
import fr.paris.lutece.plugins.directory.business.RecordFieldHome;
import fr.paris.lutece.plugins.directory.business.RecordHome;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.ConfigProducerService;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.DirectoryPDFProducerPlugin;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.utils.PDFUtils;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.directory.service.directorysearch.DirectorySearchService;
import fr.paris.lutece.plugins.directory.utils.DirectoryErrorException;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.workflow.modules.archive.business.TaskArchiveConfig;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.task.Task;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.signrequest.AbstractAuthenticator;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Workflow task to generate an archive of a directory record. This task also set a download url of the archive in an entry of the record.
 * 
 */
public class TaskArchive extends Task
{

	// CONSTANTS
	public static final String EXTENSION_FILE_ZIP = ".zip";
	public static final String EXTENSION_FILE_PDF = ".pdf";
	public static final String CONSTANT_SLASH = "/";
	public static final String CONSTANT_UNDERSCORE = "_";
	public static final String CONSTANT_QUESTION_MARK = "?";
	public static final String CONSTANT_ESPERLUETTE = "&";
	public static final String CONSTANT_EQUAL = "=";

	// PROPERTIES
	private static final String PROPERTY_TASK_ARCHIVE_TITLE = "module.workflow.archive.task_archive_title";
	private static final String PROPERTY_PDF_FOLDER_PATH = "module.workflow.archive.pdf_folder_path";
	private static final String PROPERTY_ZIP_FOLDER_PATH = "module.workflow.archive.zip_folder_path";

	// PARAMETERS
	private static final String PARAM_ID_RECORD = "id_record";
	private static final String PARAM_ID_DIRECTORY = "id_directory";
	private static final String PARAM_ID_CONFIG_PRODUCER = "id_config_producer";
	private static final String PARAM_SIGNATURE = "signature";
	private static final String PARAM_TIMESTAMP = "timestamp";
	private static final String JSP_URL_DOWNLOAD_ZIP = "jsp/site/plugins/workflow/modules/archive/DoDownloadZip.jsp";

	@Autowired
	private IResourceHistoryService _resourceHistoryService;
	@Autowired
	private TaskArchiveConfigService _taskArchiveConfigService;
	@Autowired
	private ConfigProducerService _configProducerService;
	private AbstractAuthenticator _authenticator;

	/**
	 * Set the authenticator
	 * @param authenticator The authenticator
	 */
	public void setAuthenticator( AbstractAuthenticator authenticator )
	{
		_authenticator = authenticator;
	}

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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTaskFormEntries( Locale locale )
	{
		return null;
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

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processTask( int nIdResourceHistory, HttpServletRequest request, Locale locale )
	{
		Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
		Plugin pluginDirectoryPdfProducer = PluginService.getPlugin( DirectoryPDFProducerPlugin.PLUGIN_NAME );
		TaskArchiveConfig config = _taskArchiveConfigService.findByPrimaryKey( this.getId( ) );
		ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdResourceHistory );

		if ( ( config != null ) && ( resourceHistory != null ) && resourceHistory.getResourceType( ).equals( Record.WORKFLOW_RESOURCE_TYPE ) )
		{
			String strZipURL = StringUtils.EMPTY;
			// We set the url in the record
			Record record = RecordHome.findByPrimaryKey( resourceHistory.getIdResource( ), pluginDirectory );

			IEntry entry = null;
			EntryFilter entryFilter = new EntryFilter( );
			entryFilter.setPosition( config.getIdEntryDirectory( ) );
			entryFilter.setIdDirectory( config.getIdDirectory( ) );

			List<IEntry> entryList = EntryHome.getEntryList( entryFilter, pluginDirectory );
			String strIdDirectory = Integer.toString( config.getIdDirectory( ) );

			if ( ( entryList != null ) && !entryList.isEmpty( ) )
			{
				Date date = new Date( );
				String strTimestamp = Long.toString( date.getTime( ) );
				List<String> listSignatureParams = new ArrayList<String>( );
				String strIdRecord = Integer.toString( record.getIdRecord( ) );
				listSignatureParams.add( strIdDirectory );
				listSignatureParams.add( strIdRecord );
				listSignatureParams.add( Integer.toString( config.getIdPDFProducerConfig( ) ) );
				String strSignature = _authenticator.buildSignature( listSignatureParams, strTimestamp );

				UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + JSP_URL_DOWNLOAD_ZIP );
				url.addParameter( PARAM_ID_DIRECTORY, strIdDirectory );
				url.addParameter( PARAM_ID_RECORD, strIdRecord );
				url.addParameter( PARAM_ID_CONFIG_PRODUCER, config.getIdPDFProducerConfig( ) );
				url.addParameter( PARAM_SIGNATURE, strSignature );
				url.addParameter( PARAM_TIMESTAMP, strTimestamp );
				strZipURL = url.getUrl( );

				entry = EntryHome.findByPrimaryKey( entryList.get( 0 ).getIdEntry( ), pluginDirectory );
				List<RecordField> listRecordFieldResult = new ArrayList<RecordField>( );

				List<String> listValue = new ArrayList<String>( );
				listValue.add( strZipURL );
				try
				{
					entry.getRecordFieldData( record, listValue, true, false, listRecordFieldResult, locale );

					// add Indexer action
					DirectorySearchService.getInstance( ).addIndexerAction( record.getIdRecord( ), IndexerAction.TASK_MODIFY, pluginDirectory );

					// delete all record field in database associate to the entry and the record
					RecordFieldFilter filter = new RecordFieldFilter( );
					filter.setIdRecord( record.getIdRecord( ) );
					filter.setIdEntry( entry.getIdEntry( ) );
					RecordFieldHome.removeByFilter( filter, pluginDirectory );

					// insert the new record Field
					for ( RecordField recordField : listRecordFieldResult )
					{
						recordField.setRecord( record );
						RecordFieldHome.create( recordField, pluginDirectory );
					}
				}
				catch ( DirectoryErrorException e )
				{
					AppLogService.error( e );
				}
			}

			// We generate the PDF
			Directory directory = DirectoryHome.findByPrimaryKey( config.getIdDirectory( ), pluginDirectory );
			String strDirectoryName = StringUtil.replaceAccent( directory.getTitle( ) );
			String strPDFFolderPath = AppPathService.getAbsolutePathFromRelativePath( AppPropertiesService.getProperty( PROPERTY_PDF_FOLDER_PATH ) + CONSTANT_SLASH + strDirectoryName + CONSTANT_SLASH
					+ record.getIdRecord( ) );
			String strPDFFileName = strDirectoryName + CONSTANT_UNDERSCORE + record.getIdRecord( ) + EXTENSION_FILE_PDF;
			List<Integer> listIdEntryConfig = _configProducerService.loadListConfigEntry( pluginDirectoryPdfProducer, config.getIdPDFProducerConfig( ) );

			File pdfFolder = new File( strPDFFolderPath );
			if ( !pdfFolder.exists( ) )
			{
				pdfFolder.mkdirs( );
			}
			List<File> listPdfFolderFiles = new ArrayList<File>( );
			File pdfFile = new File( strPDFFolderPath + CONSTANT_SLASH + strPDFFileName );
			listPdfFolderFiles.add( pdfFile );

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
			List<IEntry> listEntry = DirectoryUtils.getFormEntries( record.getDirectory( ).getIdDirectory( ), pluginDirectory, AdminUserService.getAdminUser( request ) );
			List<IEntry> listFilteredEntry = new ArrayList<IEntry>( );
			List<IEntry> listEntrySeen = new ArrayList<IEntry>( );
			for ( IEntry entryFile : listEntry )
			{
				addFileImageEntryToList( entryFile, listFilteredEntry, listEntrySeen );
			}
			Map<String, List<RecordField>> mapIdEntryListRecordField = DirectoryUtils.getMapIdEntryListRecordField( listFilteredEntry, record.getIdRecord( ), pluginDirectory );
			for ( List<RecordField> listRecordField : mapIdEntryListRecordField.values( ) )
			{
				for ( RecordField recordField : listRecordField )
				{
					if ( recordField.getFile( ) != null && !StringUtils.isEmpty( recordField.getFile( ).getTitle( ) ) && !( recordField.isBigThumbnail( ) || recordField.isLittleThumbnail( ) ) )
					{
						File recordFile = new File( strPDFFolderPath + CONSTANT_SLASH + recordField.getFile( ).getTitle( ) );
						listPdfFolderFiles.add( recordFile );
						try
						{
							os = new FileOutputStream( recordFile );
							PhysicalFile physicalFile = PhysicalFileHome.findByPrimaryKey( recordField.getFile( ).getPhysicalFile( ).getIdPhysicalFile( ), pluginDirectory );
							os.write( physicalFile.getValue( ) );
							os.flush( );
						}
						catch ( FileNotFoundException e )
						{
							AppLogService.error( e.getMessage( ), e );
						}
						catch ( IOException e )
						{
							AppLogService.error( e.getMessage( ), e );
						}
						finally
						{
							IOUtils.closeQuietly( os );
						}
					}
				}
			}
			// We generate the ZIP
			String strZipFolderPath = AppPathService.getAbsolutePathFromRelativePath( AppPropertiesService.getProperty( PROPERTY_ZIP_FOLDER_PATH ) );
			String strZIPFileName = strIdDirectory + CONSTANT_UNDERSCORE + record.getIdRecord( ) + EXTENSION_FILE_ZIP;
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
			for ( File file : listPdfFolderFiles )
			{
				try
				{
					file.delete( );
				}
				catch ( Exception e )
				{
					AppLogService.error( e.getMessage( ), e );
				}
			}
		}
	}

	/**
	 * Check if an entry is a file or image entry, and add it to a result list.
	 * @param entry The entry to check
	 * @param listResult The result list
	 * @param listEntrySeen The list of entry considered. This parameter is used to avoid infinite loop.
	 */
	private void addFileImageEntryToList( IEntry entry, List<IEntry> listResult, List<IEntry> listEntrySeen )
	{
		if ( !listEntrySeen.contains( entry ) )
		{
			listEntrySeen.add( entry );
			if ( entry.getChildren( ) != null && entry.getChildren( ).size( ) > 0 )
			{
				for ( IEntry entryFileChildren : entry.getChildren( ) )
				{
					addFileImageEntryToList( entryFileChildren, listResult, listEntrySeen );
				}
			}
			else if ( entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeFile || entry instanceof fr.paris.lutece.plugins.directory.business.EntryTypeImg )
			{
				listResult.add( entry );
			}
		}
	}
}
