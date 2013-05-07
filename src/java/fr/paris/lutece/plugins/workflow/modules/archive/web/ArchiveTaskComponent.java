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
package fr.paris.lutece.plugins.workflow.modules.archive.web;

import fr.paris.lutece.plugins.directory.business.DirectoryHome;
import fr.paris.lutece.plugins.directory.business.EntryFilter;
import fr.paris.lutece.plugins.directory.business.EntryHome;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.business.producerconfig.ConfigProducer;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.ConfigProducerService;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.DirectoryPDFProducerPlugin;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.workflow.modules.archive.business.TaskArchiveConfig;
import fr.paris.lutece.plugins.workflow.modules.archive.service.TaskArchiveConfigService;
import fr.paris.lutece.plugins.workflow.utils.WorkflowUtils;
import fr.paris.lutece.plugins.workflow.web.task.AbstractTaskComponent;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Archive task component
 * 
 */
public class ArchiveTaskComponent extends AbstractTaskComponent
{
	// TEMPLATES
	private static final String TEMPLATE_TASK_ARCHIVE_CONFIG = "admin/plugins/workflow/modules/archive/task_archive_config.html";

	// MARKS
	private static final String MARK_CONFIG = "config";
	private static final String MARK_DIRECTORY_LIST = "list_directory";
	private static final String MARK_DIRECTORY_ENTRY_LIST = "list_entry_directory";
	private static final String MARK_PDFPRODUCER_CONFIG_LIST = "list_pdfproducer_config";

	// MESSAGES
	private static final String MESSAGE_MANDATORY_FIELD = "module.workflow.archive.message.mandatory.field";
	private static final String MESSAGE_ARCHIVE_GENERATED = "module.workflow.archive.message.archiveGenerated";

	// PARAMETERS
	private static final String PARAMETER_APPLY = "apply";
	private static final String PARAMETER_ID_DIRECTORY = "id_directory";
	private static final String PARAMETER_ID_ENTRY_DIRECTORY = "id_entry_directory";
	private static final String PARAMETER_ID_PDFPRODUCER_CONFIG = "id_pdfproducer_config";

	// PROPERTIES
	private static final String LABEL_REFERENCE_DIRECTORY = "module.workflow.archive.task_archive_config.label_reference_directory";
	private static final String PROPERTY_ID_ENTRY_TYPE_URL = "workflow-archive.id_entry_type_url";
	private static final String FIELD_TASK_DIRECTORY = "module.workflow.archive.task_archive_config.label_task_directory";
	private static final String FIELD_TASK_ENTRY_DIRECTORY = "module.workflow.archive.task_archive_config.label_task_entry_directory";
	private static final String FIELD_TASK_PDFPRODUCER_CONFIG = "module.workflow.archive.task_archive_config.label_task_pdfproducer_config";

	// CONSTANT
	private static final String TYPE_CONFIG_PDF = "PDF";

	@Autowired
	private TaskArchiveConfigService _taskArchiveConfigService;
	@Autowired
	private ConfigProducerService _configProducerService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String doValidateTask( int nIdResource, String strResourceType, HttpServletRequest request, Locale locale, ITask task )
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayConfigForm( HttpServletRequest request, Locale locale, ITask task )
	{
		TaskArchiveConfig config = _taskArchiveConfigService.findByPrimaryKey( task.getId( ) );
		Map<String, Object> model = new HashMap<String, Object>( );
		ReferenceList entryList = null;

		Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
		Plugin pluginDirectoryPdfProducer = PluginService.getPlugin( DirectoryPDFProducerPlugin.PLUGIN_NAME );

		ReferenceList directoryList = DirectoryHome.getDirectoryList( pluginDirectory );
		ReferenceList taskReferenceListDirectory = new ReferenceList( );
		taskReferenceListDirectory.addItem( DirectoryUtils.CONSTANT_ID_NULL, "" );
		ReferenceList configProducerList = new ReferenceList( );
		configProducerList.addItem( DirectoryUtils.CONSTANT_ID_NULL, "" );
		if ( ( config != null ) && ( config.getIdDirectory( ) != DirectoryUtils.CONSTANT_ID_NULL ) )
		{
			EntryFilter entryFilter = new EntryFilter( );
			entryFilter.setIdDirectory( config.getIdDirectory( ) );
			entryFilter.setIsGroup( EntryFilter.FILTER_FALSE );
			entryFilter.setIsComment( EntryFilter.FILTER_FALSE );
			entryFilter.setIdType( AppPropertiesService.getPropertyInt( PROPERTY_ID_ENTRY_TYPE_URL, 0 ) );

			entryList = new ReferenceList( );

			for ( IEntry entry : EntryHome.getEntryList( entryFilter, pluginDirectory ) )
			{
				entryList.addItem( entry.getPosition( ), String.valueOf( entry.getPosition( ) ) + " (" + I18nService.getLocalizedString( LABEL_REFERENCE_DIRECTORY, locale ) + " : " + entry.getTitle( )
						+ ")" );
			}

			for ( ConfigProducer configProducer : _configProducerService.loadListProducerConfig( pluginDirectoryPdfProducer, config.getIdDirectory( ), TYPE_CONFIG_PDF ) )
			{
				configProducerList.addItem( configProducer.getIdProducerConfig( ), configProducer.getName( ) );
			}
		}

		if ( directoryList != null )
		{
			taskReferenceListDirectory.addAll( directoryList );
		}

		ReferenceList taskReferenceListEntry = new ReferenceList( );
		taskReferenceListEntry.addItem( DirectoryUtils.CONSTANT_ID_NULL, StringUtils.EMPTY );

		if ( entryList != null )
		{
			taskReferenceListEntry.addAll( entryList );
		}

		if ( config == null )
		{
			config = new TaskArchiveConfig( );
			config.setIdDirectory( DirectoryUtils.CONSTANT_ID_NULL );
		}

		model.put( MARK_CONFIG, config );
		model.put( MARK_DIRECTORY_ENTRY_LIST, taskReferenceListEntry );
		model.put( MARK_DIRECTORY_LIST, directoryList );
		model.put( MARK_PDFPRODUCER_CONFIG_LIST, configProducerList );

		HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_TASK_ARCHIVE_CONFIG, locale, model );

		return template.getHtml( );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayTaskForm( int nIdResource, String strResourceType, HttpServletRequest request, Locale locale, ITask task )
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayTaskInformation( int nIdHistory, HttpServletRequest request, Locale locale, ITask task )
	{
		return I18nService.getLocalizedString( MESSAGE_ARCHIVE_GENERATED, locale );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTaskInformationXml( int nIdHistory, HttpServletRequest request, Locale locale, ITask task )
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String doSaveConfig( HttpServletRequest request, Locale locale, ITask task )
	{
		String strIdDirectory = request.getParameter( PARAMETER_ID_DIRECTORY );
		String strIdEntryDirectory = request.getParameter( PARAMETER_ID_ENTRY_DIRECTORY );
		int nIdEntryDirectory = WorkflowUtils.convertStringToInt( strIdEntryDirectory );
		String strIdPDFProducerConfig = request.getParameter( PARAMETER_ID_PDFPRODUCER_CONFIG );
		int nIdPDFProducerConfig = WorkflowUtils.convertStringToInt( strIdPDFProducerConfig );
		String strError = StringUtils.EMPTY;

		if ( ( strIdDirectory == null ) || strIdDirectory.trim( ).equals( WorkflowUtils.EMPTY_STRING ) )
		{
			strError = FIELD_TASK_DIRECTORY;
		}
		else if ( ( request.getParameter( PARAMETER_APPLY ) == null ) && ( nIdEntryDirectory == WorkflowUtils.CONSTANT_ID_NULL ) )
		{
			strError = FIELD_TASK_ENTRY_DIRECTORY;
		}
		else if ( ( request.getParameter( PARAMETER_APPLY ) == null ) && ( nIdPDFProducerConfig == WorkflowUtils.CONSTANT_ID_NULL ) )
		{
			strError = FIELD_TASK_PDFPRODUCER_CONFIG;
		}

		if ( !strError.equals( WorkflowUtils.EMPTY_STRING ) )
		{
			Object[] tabRequiredFields =
			{ I18nService.getLocalizedString( strError, locale ) };

			return AdminMessageService.getMessageUrl( request, MESSAGE_MANDATORY_FIELD, tabRequiredFields, AdminMessage.TYPE_STOP );
		}

		TaskArchiveConfig config = _taskArchiveConfigService.findByPrimaryKey( task.getId( ) );
		Boolean bCreate = false;

		if ( config == null )
		{
			config = new TaskArchiveConfig( );
			bCreate = true;
		}

		config.setIdTask( task.getId( ) );
		config.setIdDirectory( DirectoryUtils.convertStringToInt( strIdDirectory ) );
		config.setIdEntryDirectory( nIdEntryDirectory );
		config.setIdPDFProducerConfig( nIdPDFProducerConfig );

		if ( bCreate )
		{
			_taskArchiveConfigService.create( config );
		}
		else
		{
			_taskArchiveConfigService.update( config );
		}
		return null;
	}
}
