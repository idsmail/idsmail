package pl.com.ids.domain;

public class Configuration<S> {
    private S specific;
    private FetchOptions fetchOptions;

    public Configuration(FetchOptions fetchOptions, S specific) {
        this.fetchOptions = fetchOptions;
        this.specific = specific;
    }

    public FetchOptions getFetchOptions() {
        return fetchOptions;
    }

    public S getSpecific() {
        return specific;
    }
}
