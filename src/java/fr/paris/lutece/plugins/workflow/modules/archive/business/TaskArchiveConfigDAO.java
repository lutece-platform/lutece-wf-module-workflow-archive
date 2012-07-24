package fr.paris.lutece.plugins.workflow.modules.archive.business;

import fr.paris.lutece.plugins.workflow.modules.archive.service.WorkflowArchivePlugin;
import fr.paris.lutece.util.sql.DAOUtil;


public class TaskArchiveConfigDAO implements ITaskArchiveConfigDAO
{

	private static final String SQL_QUERY_SELECT = "SELECT id_task, id_directory, id_entry_directory, id_pdfproducer_config FROM task_archive_cf WHERE id_task = ? ";
	private static final String SQL_QUERY_INSERT = "INSERT INTO task_archive_cf (id_task, id_directory, id_entry_directory, id_pdfproducer_config) VALUES ( ?, ?, ?, ?) ";
	private static final String SQL_QUERY_UPDATE = "UPDATE task_archive_cf SET id_directory = ?, id_entry_directory = ?, id_pdfproducer_config = ? WHERE id_task = ? ";
	private static final String SQL_QUERY_DELETE = "DELETE FROM task_archive_cf WHERE id_task = ? ";

	@Override
	public void delete( int nIdTask )
	{
		DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, WorkflowArchivePlugin.getPlugin( ) );
		daoUtil.setInt( 1, nIdTask );
		daoUtil.executeUpdate( );
		daoUtil.free( );
	}

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
