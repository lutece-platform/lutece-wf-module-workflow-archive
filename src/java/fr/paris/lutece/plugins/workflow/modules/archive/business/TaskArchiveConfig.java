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

import fr.paris.lutece.plugins.workflowcore.business.config.TaskConfig;


/**
 * Configuration of the archive workflow task
 * 
 */
public class TaskArchiveConfig extends TaskConfig
{
	private int _nIdDirectory;
	private int _nIdEntryDirectory;
	private int _nIdPDFProducerConfig;

	/**
	 * Get the id of the directory associated to the task
	 * @return The id of the directory associated to the task
	 */
	public int getIdDirectory( )
	{
		return _nIdDirectory;
	}

	/**
	 * Set the id of the directory associated to the task
	 * @param nIdDirectory the new directory id
	 */
	public void setIdDirectory( int nIdDirectory )
	{
		_nIdDirectory = nIdDirectory;
	}

	/**
	 * Get the id of the entry of the directory to set the archive download URL when processing the task
	 * @return The archive download URL
	 */
	public int getIdEntryDirectory( )
	{
		return _nIdEntryDirectory;
	}

	/**
	 * Set the id of the entry of the directory to set the archive download URL when processing the task
	 * @param nIdEntryDirectory The new archive download URL
	 */
	public void setIdEntryDirectory( int nIdEntryDirectory )
	{
		_nIdEntryDirectory = nIdEntryDirectory;
	}

	/**
	 * Get the id of the pdf producer configuration to use
	 * @return the id of the pdf producer configuration to use
	 */
	public int getIdPDFProducerConfig( )
	{
		return _nIdPDFProducerConfig;
	}

	/**
	 * Set the id of the pdf producer configuration to use
	 * @param nIdPDFProducerConfig New pdf producer configuration id
	 */
	public void setIdPDFProducerConfig( int nIdPDFProducerConfig )
	{
		_nIdPDFProducerConfig = nIdPDFProducerConfig;
	}
}
