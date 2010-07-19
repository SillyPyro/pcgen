/*
 * LanguageLoader.java
 * Copyright 2001 (C) Bryan McRoberts <merton_monk@yahoo.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on February 22, 2002, 10:29 PM
 *
 * $Id$
 */
package pcgen.persistence.lst;

import java.util.Date;
import java.util.StringTokenizer;

import pcgen.cdom.enumeration.ObjectKey;
import pcgen.core.Language;
import pcgen.core.SettingsHandler;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.SystemLoader;
import pcgen.rules.context.LoadContext;
import pcgen.util.Logging;

/**
 *
 * @author  David Rice <david-pcgen@jcuz.com>
 * @version $Revision$
 */
final class LanguageLoader extends LstObjectFileLoader<Language>
{
	/** Creates a new instance of LanguageLoader */
	public LanguageLoader()
	{
		super();
	}

	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#parseLine(LoadContext, pcgen.core.PObject, java.lang.String, pcgen.persistence.lst.CampaignSourceEntry)
	 */
	@Override
	public Language parseLine(LoadContext context, Language aLang,
		String lstLine, SourceEntry source) throws PersistenceLayerException
	{
		Language lang = aLang;

		boolean isnew = false;
		if (lang == null)
		{
			lang = new Language();
			isnew = true;
		}

		final StringTokenizer colToken =
				new StringTokenizer(lstLine, SystemLoader.TAB_DELIM);

		if (colToken.hasMoreTokens())
		{
			lang.setName(colToken.nextToken());
			lang.put(ObjectKey.SOURCE_CAMPAIGN, source.getCampaign());
			lang.setSourceURI(source.getURI());
			if (isnew)
			{
				context.addStatefulInformation(lang);
				context.ref.importObject(lang);
			}
		}

		while (colToken.hasMoreTokens())
		{
			final String token = colToken.nextToken().trim();
			final int colonLoc = token.indexOf(':');
			if (colonLoc == -1)
			{
				Logging
						.errorPrint("Invalid Token - does not contain a colon: '"
								+ token
								+ "' in Language "
								+ lang.getDisplayName() + " of " + source);
				continue;
			}
			else if (colonLoc == 0)
			{
				Logging.errorPrint("Invalid Token - starts with a colon: '"
						+ token + "' in Language " + lang.getDisplayName()
						+ " of " + source);
				continue;
			}

			String key = token.substring(0, colonLoc);
			String value = (colonLoc == token.length() - 1) ? null : token
					.substring(colonLoc + 1);
			if (context.processToken(lang, key, value))
			{
				context.commit();
			}
			else
			{
				context.rollback();
				Logging.replayParsedMessages();
 			}
			Logging.clearParseMessages();
		}

		completeObject(context, source, lang);
		return null;
	}

	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#getObjectKeyed(LoadContext, java.lang.String)
	 */
	@Override
	protected Language getObjectKeyed(LoadContext context, String aKey)
	{
		return context.ref.silentlyGetConstructedCDOMObject(Language.class, aKey);
	}

}
