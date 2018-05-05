package com.alium.nibo.di;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.alium.nibo.repo.contracts.IGeoCodingRepository;
import com.alium.nibo.repo.contracts.ISuggestionRepository;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by aliumujib on 03/05/2018.
 */

public class Injection {


    private RepositoryModule repositoryModule;
    private GoogleClientModule googleClientModule;
    private ProviderModule providerModule;


    public GoogleApiClient getGoogleApiClient() {
        return googleClientModule.getGoogleApiClient();
    }

    private Injection() {
    }

    public Injection(RepositoryModule repositoryModule, GoogleClientModule googleClientModule, ProviderModule providerModule) {
        this.repositoryModule = repositoryModule;
        this.googleClientModule = googleClientModule;
        this.providerModule = providerModule;
    }

    public IGeoCodingRepository getGeoCodingRepository() {
        return repositoryModule.getGeoCodingRepository();
    }


    public ISuggestionRepository getSuggestionsRepository() {
        return providerModule.getSuggestionsProvider();
    }


    public static class InjectionBuilder {
        private APIModule apiModule;
        private RepositoryModule repositoryModule;
        private GoogleClientModule googleClientModule;
        private RetrofitModule retrofitModule;
        private ProviderModule providerModule;
        private Context context;

        public InjectionBuilder setAPIModule(APIModule apiModule) {
            this.apiModule = apiModule;
            return this;
        }

        public InjectionBuilder setRepositoryModule(RepositoryModule repositoryModule) {
            this.repositoryModule = repositoryModule;
            return this;
        }

        public InjectionBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        public InjectionBuilder setGoogleClientModule(GoogleClientModule googleClientModule) {
            this.googleClientModule = googleClientModule;
            return this;
        }

        public InjectionBuilder setRetrofitModule(RetrofitModule retrofitModule) {
            this.retrofitModule = retrofitModule;
            return this;
        }

        public InjectionBuilder setProviderModule(ProviderModule providerModule) {
            this.providerModule = providerModule;
            return this;
        }

        public Injection build() {
            if (googleClientModule == null) {
                throw new IllegalStateException("Please set GoogleAPI module, it has external dependencies");
            } else if (providerModule == null) {
                if (context == null) {
                    throw new IllegalStateException("Please set context, it has external dependencies");
                } else {
                    providerModule = new ProviderModule(googleClientModule.getGoogleApiClient(), context);
                }
            } else if (retrofitModule == null) {
                retrofitModule = RetrofitModule.getInstance();
            } else if (apiModule == null) {
                apiModule = APIModule.getInstance(retrofitModule);
            } else if (repositoryModule == null) {
                repositoryModule = RepositoryModule.getInstance(apiModule);
            }

            return new Injection(repositoryModule, googleClientModule, providerModule);
        }
    }


}
