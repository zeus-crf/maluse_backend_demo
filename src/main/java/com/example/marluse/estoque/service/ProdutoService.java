package com.example.marluse.estoque.service;

import com.example.marluse.estoque.dto.CategoriaProduto;
import com.example.marluse.estoque.dto.ProdutoAtualizarRequest;
import com.example.marluse.estoque.dto.ProdutoFornecedorRequest;
import com.example.marluse.estoque.dto.ProdutoRequest;
import com.example.marluse.estoque.dto.ProdutoResponse;
import com.example.marluse.estoque.enums.UnidadeMedida;
import com.example.marluse.estoque.model.Fornecedor;
import com.example.marluse.estoque.model.Produto;
import com.example.marluse.estoque.model.ProdutoFornecedor;
import com.example.marluse.estoque.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final FornecedorService fornecedorService;


    @Transactional
    public ProdutoResponse criar(ProdutoRequest request){
        if (produtoRepository.findByNome(request.nome()).isPresent()){
            throw new IllegalArgumentException("Esse produto já exite");
        }

        Produto produto = Produto.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .preco(request.preco())
                .precoDiaria(request.precoDiaria() != null ? request.precoDiaria() : request.preco())
                .valorCompra(request.valorCompra())
                .quantidadeEstoque(request.quantidadeEstoque() != null ? request.quantidadeEstoque() : BigDecimal.ZERO)
                .estoqueMinimo(request.estoqueMinimo() != null ? request.estoqueMinimo() : 0)
                .ativo(true)
                .medida(request.medida())
                .categoria(request.categoria() != null ? request.categoria() : CategoriaProduto.OUTROS)
                .build();

        aplicarFornecedores(produto, request.fornecedores());

        return ProdutoResponse.from(produtoRepository.save(produto));

    }

    /**
     * Aplica ao produto a lista de linhas fornecedor+preço vinda da requisição.
     * Substitui o conjunto inteiro: limpa os vínculos atuais (orphanRemoval apaga
     * do banco) e recria a partir dos nomes, resolvendo cada fornecedor (criar-na-hora).
     * Linha nula ou sem nome é ignorada.
     */
    private void aplicarFornecedores(Produto produto, List<ProdutoFornecedorRequest> linhas) {
        produto.limparFornecedores();
        if (linhas == null) return;
        for (ProdutoFornecedorRequest linha : linhas) {
            if (linha == null || linha.nome() == null || linha.nome().isBlank()) continue;
            Fornecedor fornecedor = fornecedorService.resolverPorNomes(List.of(linha.nome()))
                    .iterator().next();
            produto.addFornecedor(ProdutoFornecedor.builder()
                    .fornecedor(fornecedor)
                    .precoCompra(linha.precoCompra())
                    .build());
        }
    }

    @Transactional
    public Produto criarRascunho( String nome, BigDecimal preco, BigDecimal precoDiaria){

        BigDecimal precoVenda = preco != null ? preco :
                BigDecimal.ZERO;

        Produto produto = Produto.builder()
                .nome(nome)
                .preco(precoVenda)
                .precoDiaria(precoDiaria != null ? precoDiaria : precoVenda)
                .valorCompra(BigDecimal.ZERO)
                .quantidadeEstoque(BigDecimal.ZERO)
                .estoqueMinimo(0)
                .ativo(true)
                .rascunho(true)
                .medida(UnidadeMedida.PECA)
                .categoria(CategoriaProduto.OUTROS)
                .build();

        return produtoRepository.save(produto);


    }

    public List<ProdutoResponse> listar(){
        return produtoRepository.findByAtivoTrueAndRascunhoFalse()
                .stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    public ProdutoResponse burcarPorId(String id){
        return produtoRepository.findById(id)
                .map(ProdutoResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    public List<ProdutoResponse> listarRascunho() {
        return produtoRepository.findByAtivoTrueAndRascunhoTrue()
                .stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    public ProdutoResponse atualizar(String id, ProdutoAtualizarRequest request){
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        if (request.nome() != null) produto.setNome(request.nome());
        if (request.descricao() != null) produto.setDescricao(request.descricao());
        if (request.valorCompra() != null) produto.setValorCompra(request.valorCompra());
        if (request.preco() != null) produto.setPreco(request.preco());
        if (request.precoDiaria() != null) produto.setPrecoDiaria(request.precoDiaria());
        if (request.medida() != null) produto.setMedida(request.medida());
        if (request.quantidadeEstoque() != null) produto.setQuantidadeEstoque(request.quantidadeEstoque());
        if (request.categoria() != null ) produto.setCategoria(request.categoria());
        if (request.fornecedores() != null) aplicarFornecedores(produto, request.fornecedores());

        produto.setEstoqueMinimo(request.estoqueMinimo() != null ? request.estoqueMinimo() : 0);

        produto.setRascunho(false);

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    public List<ProdutoResponse> listarEstoqueBaixo() {
        return produtoRepository.findEstoqueBaixo()
                .stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    public void inativar(String id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }
}
