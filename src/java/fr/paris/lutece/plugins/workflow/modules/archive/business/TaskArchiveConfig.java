package fr.paris.lutece.plugins.workflow.modules.archive.business;

import fr.paris.lutece.plugins.workflowcore.business.config.TaskConfig;


public class TaskArchiveConfig extends TaskConfig
{
	private int _nIdDirectory;
	private int _nIdEntryDirectory;
	private int _nIdPDFProducerConfig;

	public int getIdDirectory( )
	{
		return _nIdDirectory;
	}

	public void setIdDirectory( int nIdDirectory )
	{
		_nIdDirectory = nIdDirectory;
	}

	public int getIdEntryDirectory( )
	{
		return _nIdEntryDirectory;
	}

	public void setIdEntryDirectory( int nIdEntryDirectory )
	{
		_nIdEntryDirectory = nIdEntryDirectory;
	}

	public int getIdPDFProducerConfig( )
	{
		return _nIdPDFProducerConfig;
	}

	public void setIdPDFProducerConfig( int nIdPDFProducerConfig )
	{
		_nIdPDFProducerConfig = nIdPDFProducerConfig;
	}
}
