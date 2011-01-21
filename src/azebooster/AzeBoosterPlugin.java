/*
 *  (c) Copyright (c) 2010 Mridang Agarwalla
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package azebooster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.net.MalformedURLException;

import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginConfig;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.*;
import org.gudy.azureus2.plugins.logging.*;
import org.gudy.azureus2.plugins.torrent.*;
import org.gudy.azureus2.plugins.ui.menus.*;
import org.gudy.azureus2.ui.swt.plugins.*;

public class AzeBoosterPlugin implements Plugin {

    private LoggerChannel channel;
    private PluginInterface pluginI;
    private TorrentAttribute doBoost;
    private MenuManager menuMan;
    private MenuItem itmBoostTrackers;

    public class BoostMenu implements MenuItemFillListener, MenuItemListener {

       /**
        * This listener for the boost menu item's click event
        *
        * @param   item    the menu item
        * @param   context the menu item's context
        * @returns
        */
        public void selected(MenuItem item, Object context) {
            Download dls = (Download)context;
            if (item.isSelected()) {
                if (addTrackers(dls)) {
                    dls.setAttribute(doBoost, "true");
                    item.setEnabled(false);
                }
            }
            else {
                dls.setAttribute(doBoost, "false");
                item.setEnabled(false);
            }
        }

       /**
        * This listener for the boost menu item's show event
        *
        * @param   item    the menu item
        * @param   context the menu item's context
        * @returns
        */
        public void menuWillBeShown(MenuItem item, Object context) {
            Object[] dls = (Object[])context;
            if (((Download)dls[0]).getAttribute(doBoost).equals("true")) {
                item.setData(true);
                item.setEnabled(false);
            }
            else {
                item.setData(false);
                item.setEnabled(true);
            }
        }

    }

   /**
    * This procedure handle the initialisation of the plugin like configuring
    * loggers, setting up UI widgets, etc.
    *
    * @param   pluginI The plugin manager
    * @returns
    */
    public void initialize(final PluginInterface pluginI) {
        this.pluginI = pluginI;
        this.menuMan = pluginI.getUIManager().getMenuManager();

        this.doBoost = pluginI.getTorrentManager().getPluginAttribute("boost");
        this.channel = pluginI.getLogger().getChannel("azexec");

        this.itmBoostTrackers = this.menuMan.addMenuItem(MenuManager.MENU_DOWNLOAD_CONTEXT, "azexec.menu.boost_trackers");
        this.itmBoostTrackers.setStyle(MenuItem.STYLE_CHECK);
        this.itmBoostTrackers.setData(false);
        this.itmBoostTrackers.addListener(new BoostMenu());
        this.itmBoostTrackers.addFillListener(new BoostMenu());
    }

   /**
    * This adds the trackers to the torrent. It doesn't do any checking if the
    * tracker already exists in the torrent. It simply adds them.
    *
    * @param   downloadItem The item on which the menu has been opened
    * @returns
    */
    private Boolean addTrackers(Download downloadItem) {
        try {
            Torrent downloadTorrent = downloadItem.getTorrent();
            TorrentAnnounceURLList downloadURLs = downloadTorrent.getAnnounceURLList();
            downloadURLs.addSet(getTrackers());
            return true;
        }
        catch (Exception e) {
            channel.logAlert(LoggerChannel.LT_ERROR, "An error occurred while trying to add more trackers.");
            return false;
        }
    }

   /**
    * This fetches the URLs from the defined website and then puts them into an
    * array to be added to the torrent
    *
    * @returns an array of URLs fetched from the site to be added to the torrent
    */
    private URL[] getTrackers() throws Exception {
        try {
            URL httpURL;
            URLConnection httpFetcher;
            BufferedReader httpBuffer;
            InputStreamReader httpStream;
            String httpResponseLine;
            List<URL> trackerURLs = new ArrayList<URL>();

            httpURL = new URL("http://www.trackon.org/api/live");
            httpFetcher = httpURL.openConnection();
            httpStream = new InputStreamReader(httpFetcher.getInputStream());
            httpBuffer = new BufferedReader(httpStream);
            while ((httpResponseLine = httpBuffer.readLine()) != null) {
                trackerURLs.add(new URL(httpResponseLine));
            }

            return trackerURLs.toArray(new URL[]{});
        }
        catch (MalformedURLException e) {
            throw e;
        }
        catch (IOException e) {
            throw e;
        }
    }

}