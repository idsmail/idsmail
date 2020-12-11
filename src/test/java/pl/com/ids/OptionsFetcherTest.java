package pl.com.ids;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import pl.com.ids.application.standard.OptionsFetcher;
import pl.com.ids.domain.Configuration;
import pl.com.ids.application.standard.Service;
import pl.com.ids.infrastructure.IdsLogger;
import pl.com.ids.infrastructure.SimpleDebugger;

import static org.junit.Assert.*;

public class OptionsFetcherTest {
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void whenArgumentIPassedThenProtocolImaps() throws Exception {
        //given
        String[] args = {"mail_server_name", "user",  "pass", String.valueOf(folder.getRoot()),  "-i"};

        //when
        IdsLogger l = new IdsLogger("temp.log");
        OptionsFetcher optionsFetcher = new OptionsFetcher(args);
        Configuration<Service> fetchOptions = optionsFetcher.getFetchOptions(l, new SimpleDebugger());

        //then
        assertEquals(fetchOptions.getSpecific().getProtocol(), Protocol.IMAPS);
    }

    @Test
    public void whenArgumentEPassedThenProtocolPop3s() throws Exception {
        //given
        String[] args = {"mail_server_name", "user",  "pass", String.valueOf(folder.getRoot()),  "-e"};

        //when
        IdsLogger l = new IdsLogger("temp.log");
        OptionsFetcher optionsFetcher = new OptionsFetcher(args);
        Configuration<Service> fetchOptions = optionsFetcher.getFetchOptions(l, new SimpleDebugger());

        //then
        Service service = fetchOptions.getSpecific();
        assertEquals(service.getUrl().toString(), "pop3s://user:pass@mail_server_name/INBOX");
    }

    @Test
    public void whenArgumentPPassedThenPortIsOverriden() throws Exception {
        //given
        String[] args = {"mail_server_name", "user",  "pass", String.valueOf(folder.getRoot()),  "-e", "-p340"};

        //when
        IdsLogger l = new IdsLogger("temp.log");
        OptionsFetcher optionsFetcher = new OptionsFetcher(args);
        Configuration<Service> fetchOptions = optionsFetcher.getFetchOptions(l, new SimpleDebugger());

        //then
        Service service = fetchOptions.getSpecific();
        assertEquals(service.getUrl().toString(), "pop3s://user:pass@mail_server_name:340/INBOX");
    }

}