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
package fr.paris.lutece.plugins.workflow.modules.archive.business;

import fr.paris.lutece.plugins.workflow.modules.archive.service.WorkflowArchivePlugin;
import fr.paris.lutece.util.sql.DAOUtil;


/**
 * Implementation of the ITaskArchiveConfigDAO interface.
 * 
 */
public class TaskArchiveConfigDAO implements ITaskArchiveConfigDAO
{

	private static final String SQL_QUERY_SELECT = "SELECT id_task, id_directory, id_entry_directory, id_pdfproducer_config FROM task_archive_cf WHERE id_task = ? ";
	private static final String SQL_QUERY_INSERT = "INSERT INTO task_archive_cf (id_task, id_directory, id_entry_directory, id_pdfproducer_config) VALUES ( ?, ?, ?, ?) ";
	private static final String SQL_QUERY_UPDATE = "UPDATE task_archive_cf SET id_directory = ?, id_entry_directory = ?, id_pdfproducer_config = ? WHERE id_task = ? ";
	private static final String SQL_QUERY_DELETE = "DELETE FROM task_archive_cf WHERE id_task = ? ";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete( int nIdTask )
	{
		DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, WorkflowArchivePlugin.getPlugin( ) );
		daoUtil.setInt( 1, nIdTask );
		daoUtil.executeUpdate( );
		daoUtil.free( );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insert( TaskArchiveConfig config )
	{
		DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, WorkflowArchivePlugin.getPlugin( ) );
		daoUtil.setInt( 1, config.getIdTask( ) );
		daoUtil.setInt( 2, config.getIdDirectory( ) );
		daoUtil.setInt( 3, config.getIdEntryDirectory( ) );
		daoUtil.setInt( 4, config.getIdPDFProducerConfig( ) );

		daoUtil.executeUpdate( );
		daoUtil.free( );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskArchiveConfig load( int nIdTask )
	{
		DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, WorkflowArchivePlugin.getPlugin( ) );
		daoUtil.setInt( 1, nIdTask );

		TaskArchiveConfig taskArchiveConfig = null;

		daoUtil.executeQuery( );

		if ( daoUtil.next( ) )
		{
			taskArchiveConfig = new TaskArchiveConfig( );
			taskArchiveConfig.setIdTask( daoUtil.getInt( 1 ) );
			taskArchiveConfig.setIdDirectory( daoUtil.getInt( 2 ) );
			taskArchiveConfig.setIdEntryDirectory( daoUtil.getInt( 3 ) );
			taskArchiveConfig.setIdPDFProducerConfig( daoUtil.getInt( 4 ) );
		}
		daoUtil.free( );
		return taskArchiveConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store( TaskArchiveConfig config )
	{
		DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, WorkflowArchivePlugin.getPlugin( ) );

		daoUtil.setInt( 1, config.getIdDirectory( ) );
		daoUtil.setInt( 2, config.getIdEntryDirectory( ) );
		daoUtil.setInt( 3, config.getIdPDFProducerConfig( ) );
		daoUtil.setInt( 4, config.getIdTask( ) );

		daoUtil.executeUpdate( );
		daoUtil.free( );
	}

}
