package br.com.digitalhouse.mercadolivreapp.tairo.repository;

import android.content.Context;

import java.util.List;

import br.com.digitalhouse.mercadolivreapp.tairo.data.database.DatabaseRoom;
import br.com.digitalhouse.mercadolivreapp.tairo.data.database.dao.ResultsDao;
import br.com.digitalhouse.mercadolivreapp.tairo.data.network.RetrofitService;
import br.com.digitalhouse.mercadolivreapp.tairo.model.MercadoLivreResponse;
import br.com.digitalhouse.mercadolivreapp.tairo.model.Result;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public class MercadoLivreRepository {

    // Pega os dados da base de dados
    public Flowable<List<Result>> getLocalResults(Context context) {
        DatabaseRoom room = DatabaseRoom.getDatabase(context);
        ResultsDao resultsDao = room.resultsDAO();
        return resultsDao.getAll();
    }

    // Insere uma lista reults na base de dados
    public void insertItems(Context context, List<Result> items) {
        DatabaseRoom room = DatabaseRoom.getDatabase(context);
        ResultsDao resultsDao = room.resultsDAO();
        resultsDao.insert(items);
    }

    // Pega os items que virão da api do mercado livre
    public Observable<MercadoLivreResponse> searchItems(String item, int pagina, int limite) {
        return RetrofitService.getApiService().searchItem(item, pagina, limite);
    }
}
