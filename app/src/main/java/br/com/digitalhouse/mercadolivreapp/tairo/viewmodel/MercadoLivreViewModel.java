package br.com.digitalhouse.mercadolivreapp.tairo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import br.com.digitalhouse.mercadolivreapp.tairo.data.database.DatabaseRoom;
import br.com.digitalhouse.mercadolivreapp.tairo.data.database.dao.ResultsDao;
import br.com.digitalhouse.mercadolivreapp.tairo.model.MercadoLivreResponse;
import br.com.digitalhouse.mercadolivreapp.tairo.model.Result;
import br.com.digitalhouse.mercadolivreapp.tairo.repository.MercadoLivreRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static br.com.digitalhouse.mercadolivreapp.tairo.util.AppUtil.isNetworkConnected;

public class MercadoLivreViewModel extends AndroidViewModel {

    private MutableLiveData<List<Result>> resultLiveData = new MutableLiveData<>();
    private CompositeDisposable disposable = new CompositeDisposable();
    private MercadoLivreRepository repository = new MercadoLivreRepository();

    public MercadoLivreViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Result>> getResultLiveData() {
        return resultLiveData;
    }

    // Ao buscar o item verificamos se estamos conectados ou não
    public void searchItem(String item, int pagina, int limite) {
        if (isNetworkConnected(getApplication())) {
            getFromNetwork(item, pagina, limite);
        } else {
            getFromLocal();
        }
    }

    private void getFromLocal() {

        // Adicionamos a chamada a um disposible para podermos eliminar o disposable da destruição do viewmodel
        disposable.add(
                repository.getLocalResults(getApplication().getApplicationContext())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(subscription -> {
                    // setar livedata para mostrar loading
                })
                .doAfterTerminate(() -> {
                    // setar livedata para esconmder loading
                })
                .subscribe(results -> {
                    resultLiveData.setValue(results);
                }, throwable -> {
                    // setar livedata para mostrar error
                })
        );

    }

    private void getFromNetwork(String item, int pagina, int limite) {

        // Adicionamos a chamada a um disposible para podermos eliminar o disposable da destruição do viewmodel
        disposable.add(
                repository.searchItems(item, pagina, limite)
                        .subscribeOn(Schedulers.newThread())
                        .map(this::saveItems)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(subscription -> {
                            // setar livedata para mostrar loading
                        })
                        .doAfterTerminate(() -> {
                            // setar livedata para esconmder loading
                        })
                        .subscribe(mercadoLivreResponse -> {
                            resultLiveData.setValue(mercadoLivreResponse.getResults());
                        }, throwable -> {
                            // setar livedata para mostrar error
                        })
        );
        //Salvar os items quando tivermos buscado

    }

    private MercadoLivreResponse saveItems(MercadoLivreResponse mercadoLivreResponse) {
        ResultsDao dao = DatabaseRoom.getDatabase(getApplication().getApplicationContext())
                .resultsDAO();

                dao.deleteAll();
                dao.insert(mercadoLivreResponse.getResults());

        return mercadoLivreResponse;
    }

    // Limpa as chamadas que fizemos no RX
    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
