package br.com.techchallenge.mecanica.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.techchallenge.mecanica.domain.cliente.Cliente;
import br.com.techchallenge.mecanica.domain.cliente.valueobject.CpfCnpj;
import br.com.techchallenge.mecanica.domain.enums.StatusOrdemDeServicoEnum;
import br.com.techchallenge.mecanica.domain.enums.StatusServicoEnum;
import br.com.techchallenge.mecanica.domain.exception.CpfInvalidoException;
import br.com.techchallenge.mecanica.domain.exception.PlacaInvalidaException;
import br.com.techchallenge.mecanica.domain.exception.RegraNegocioException;
import br.com.techchallenge.mecanica.domain.operador.Operador;
import br.com.techchallenge.mecanica.domain.ordemdeservico.OrdemDeServico;
import br.com.techchallenge.mecanica.domain.peca.Peca;
import br.com.techchallenge.mecanica.domain.pecaordemdeservico.PecaOrdemDeServico;
import br.com.techchallenge.mecanica.domain.servico.Servico;
import br.com.techchallenge.mecanica.domain.servicoordemdeservico.ServicoOrdemDeServico;
import br.com.techchallenge.mecanica.domain.veiculo.Veiculo;
import br.com.techchallenge.mecanica.domain.veiculo.valueObject.Placa;

class DomainCoverageTest {

    private static final String CPF = "52998224725";

    @Test
    void clienteDeveCriarAtualizarInativarECompararPorId() {
        var id = UUID.randomUUID();
        var cpf = new CpfCnpj(CPF);
        var cliente = new Cliente(id, "Ana", cpf, "11999999999", "ana@email.com", true, null);

        assertEquals(id, cliente.getId());
        assertEquals(cpf, cliente.getCpfCnpj());
        assertTrue(cliente.isAtivo());
        assertNull(cliente.getDataInativacao());

        cliente.atualizarDados("Ana Maria", "11888888888", "ana.maria@email.com");
        assertEquals("Ana Maria", cliente.getNome());
        assertEquals("11888888888", cliente.getContato());
        assertEquals("ana.maria@email.com", cliente.getEmail());

        cliente.inativar();
        assertFalse(cliente.isAtivo());
        assertNotNull(cliente.getDataInativacao());
        assertThrows(RegraNegocioException.class, cliente::inativar);

        var mesmo = new Cliente(id, "Outra", cpf, "1", "outra@email.com", true, null);
        assertEquals(cliente, cliente);
        assertEquals(cliente, mesmo);
        assertNotEquals(cliente, new Cliente("Sem id", cpf, "1", "sem@email.com"));
        assertNotEquals(cliente, "cliente");
        assertEquals(cliente.hashCode(), mesmo.hashCode());
    }

    @Test
    void clienteDeveValidarNomeEEmailEmTodosOsFluxos() {
        var cpf = new CpfCnpj(CPF);
        assertThrows(RegraNegocioException.class, () -> new Cliente(" ", cpf, "1", "a@b.com"));
        assertThrows(RegraNegocioException.class, () -> new Cliente("Ana", cpf, "1", null));

        var cliente = new Cliente("Ana", cpf, "1", "a@b.com");
        assertThrows(RegraNegocioException.class, () -> cliente.atualizarDados(null, "2", "a@b.com"));
        assertThrows(RegraNegocioException.class, () -> cliente.atualizarDados("Ana", "2", "invalido"));
    }

    @Test
    void operadorDeveCobrirCicloDeVidaValidacoesEIgualdade() {
        var id = UUID.randomUUID();
        var operador = new Operador(id, "Bia", 10, "bia@email.com", "1199", "MECANICO", true, null);
        operador.atualizarDados("Beatriz", "beatriz@email.com", "1188", "CHEFE");

        assertEquals("Beatriz", operador.getNome());
        assertEquals(10, operador.getMatricula());
        assertEquals("CHEFE", operador.getCargo());
        assertTrue(operador.isAtivo());

        operador.inativar();
        assertFalse(operador.isAtivo());
        assertNotNull(operador.getDataInativacao());
        assertThrows(RegraNegocioException.class, operador::inativar);

        var mesmo = new Operador(id, "Outro", 11, "outro@email.com", "1", "CARGO", true, null);
        assertEquals(operador, operador);
        assertEquals(operador, mesmo);
        assertNotEquals(operador, new Operador("Sem id", 12, "sem@email.com", "1", "CARGO"));
        assertNotEquals(operador, new Object());
        assertEquals(operador.hashCode(), mesmo.hashCode());

        assertThrows(IllegalArgumentException.class,
                () -> new Operador("", 1, "a@b.com", "1", "CARGO"));
        assertThrows(IllegalArgumentException.class,
                () -> new Operador("Nome", 1, "email", "1", "CARGO"));
        assertThrows(IllegalArgumentException.class,
                () -> operador.atualizarDados(null, "a@b.com", "1", "CARGO"));
        assertThrows(IllegalArgumentException.class,
                () -> operador.atualizarDados("Nome", null, "1", "CARGO"));
    }

    @Test
    void pecaDeveCobrirCicloDeVidaValidacoesEIgualdade() {
        var id = UUID.randomUUID();
        var peca = new Peca(id, "Filtro", "Bosch", new BigDecimal("40.00"), true, null);
        peca.atualizarDados("Filtro premium", "XPTO", new BigDecimal("50.00"));

        assertEquals("Filtro premium", peca.getNome());
        assertEquals("XPTO", peca.getMarca());
        assertEquals(new BigDecimal("50.00"), peca.getPreco());

        peca.inativar();
        assertFalse(peca.isAtivo());
        assertNotNull(peca.getDataInativacao());
        assertThrows(RegraNegocioException.class, peca::inativar);

        var mesmo = new Peca(id, "Outra", "Marca", BigDecimal.ONE);
        assertEquals(peca, peca);
        assertEquals(peca, mesmo);
        assertNotEquals(peca, new Peca("Sem id", "Marca", BigDecimal.ONE));
        assertNotEquals(peca, "peca");
        assertEquals(peca.hashCode(), mesmo.hashCode());

        assertThrows(RegraNegocioException.class, () -> new Peca("", "M", BigDecimal.ONE));
        assertThrows(RegraNegocioException.class, () -> new Peca("P", "M", null));
        assertThrows(RegraNegocioException.class, () -> new Peca("P", "M", BigDecimal.ZERO));
        assertThrows(RegraNegocioException.class,
                () -> peca.atualizarDados(null, "M", BigDecimal.ONE));
        assertThrows(RegraNegocioException.class,
                () -> peca.atualizarDados("P", "M", new BigDecimal("-1")));
    }

    @Test
    void servicoDeveCobrirCicloDeVidaValidacoesEIgualdade() {
        var id = UUID.randomUUID();
        var servico = new Servico(id, "Troca", new BigDecimal("100.00"), true, null);
        servico.atualizarDados("Troca completa", new BigDecimal("120.00"));

        assertEquals("Troca completa", servico.getDescricao());
        assertEquals(new BigDecimal("120.00"), servico.getPreco());

        servico.inativar();
        assertFalse(servico.isAtivo());
        assertNotNull(servico.getDataInativacao());
        assertThrows(RegraNegocioException.class, servico::inativar);

        var mesmo = new Servico(id, "Outro", BigDecimal.ONE, true, null);
        assertEquals(servico, servico);
        assertEquals(servico, mesmo);
        assertNotEquals(servico, new Servico("Sem id", BigDecimal.ONE));
        assertNotEquals(servico, "servico");
        assertEquals(servico.hashCode(), mesmo.hashCode());

        assertThrows(RegraNegocioException.class, () -> new Servico(" ", BigDecimal.ONE));
        assertThrows(RegraNegocioException.class, () -> new Servico("Servico", null));
        assertThrows(RegraNegocioException.class, () -> new Servico("Servico", BigDecimal.ZERO));
        assertThrows(RegraNegocioException.class,
                () -> servico.atualizarDados(null, BigDecimal.ONE));
        assertThrows(RegraNegocioException.class,
                () -> servico.atualizarDados("Servico", new BigDecimal("-1")));
    }

    @Test
    void veiculoDeveCobrirCicloDeVidaValidacoesEIgualdade() {
        var id = UUID.randomUUID();
        var clienteId = UUID.randomUUID();
        var veiculo = new Veiculo(id, new Placa("ABC1D23"), "Honda", "Civic", 2020,
                clienteId, true, null);

        veiculo.atualizarDados(new Placa("DEF-1234"), "Toyota", "Corolla", 2021);
        var novoCliente = UUID.randomUUID();
        veiculo.alterarCliente(novoCliente);

        assertEquals("DEF1234", veiculo.getPlaca().getValor());
        assertEquals("Toyota", veiculo.getMarca());
        assertEquals("Corolla", veiculo.getModelo());
        assertEquals(2021, veiculo.getAno());
        assertEquals(novoCliente, veiculo.getClienteId());

        veiculo.inativar();
        assertFalse(veiculo.isAtivo());
        assertNotNull(veiculo.getDataInativacao());
        assertThrows(RegraNegocioException.class, veiculo::inativar);

        var mesmo = new Veiculo(id, new Placa("GHI1J23"), "M", "X", 2020, clienteId);
        assertEquals(veiculo, veiculo);
        assertEquals(veiculo, mesmo);
        assertNotEquals(veiculo, new Veiculo(new Placa("JKL1M23"), "M", "X", 2020, clienteId));
        assertNotEquals(veiculo, "veiculo");
        assertEquals(veiculo.hashCode(), mesmo.hashCode());

        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "", "X", 2020, clienteId));
        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "M", null, 2020, clienteId));
        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "M", "X", 1899, clienteId));
        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "M", "X", LocalDate.now().getYear() + 2, clienteId));
        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "M", "X", null, clienteId));
        assertThrows(RegraNegocioException.class,
                () -> new Veiculo(new Placa("ABC1D23"), "M", "X", 2020, null));
        assertThrows(RegraNegocioException.class, () -> veiculo.alterarCliente(null));
        assertThrows(RegraNegocioException.class,
                () -> veiculo.atualizarDados(new Placa("ABC1D23"), null, "X", 2020));
    }

    @Test
    void cpfCnpjDeveNormalizarValidarEImplementarContratoDeValor() {
        var formatado = new CpfCnpj("529.982.247-25");
        var igual = new CpfCnpj(CPF);
        var cnpj = new CpfCnpj("11.222.333/0001-81");

        assertEquals(CPF, formatado.getValor());
        assertEquals(CPF, formatado.toString());
        assertEquals(formatado, formatado);
        assertEquals(formatado, igual);
        assertEquals(formatado.hashCode(), igual.hashCode());
        assertEquals("11222333000181", cnpj.getValor());
        assertNotEquals(formatado, cnpj);
        assertNotEquals(formatado, "cpf");

        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj(null));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("123"));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("11111111111"));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("52998224724"));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("52998224715"));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("11111111111111"));
        assertThrows(CpfInvalidoException.class, () -> new CpfCnpj("11222333000180"));
    }

    @Test
    void placaDeveNormalizarValidarEImplementarContratoDeValor() {
        var placa = new Placa(" abc-1234 ");
        var igual = new Placa("ABC1234");
        var mercosul = new Placa("abc1d23");

        assertEquals("ABC1234", placa.getValor());
        assertEquals("ABC1234", placa.toString());
        assertEquals(placa, placa);
        assertEquals(placa, igual);
        assertEquals(placa.hashCode(), igual.hashCode());
        assertNotEquals(placa, mercosul);
        assertNotEquals(placa, "placa");

        assertThrows(PlacaInvalidaException.class, () -> new Placa(null));
        assertThrows(PlacaInvalidaException.class, () -> new Placa("INVALIDA"));
    }

    @Test
    void itemDePecaDeveValidarAtualizarCalcularEComparar() {
        var id = UUID.randomUUID();
        var peca = new Peca("Filtro", "Marca", BigDecimal.TEN);
        var item = new PecaOrdemDeServico(id, peca, 2, new BigDecimal("9.50"));

        assertEquals(new BigDecimal("19.00"), item.calcularValorTotal());
        var novoId = UUID.randomUUID();
        item.atualizarPecaOrdemDeServico(novoId, peca, 3, BigDecimal.TEN);
        assertEquals(novoId, item.getId());
        assertEquals(3, item.getQuantidade());
        assertEquals(new BigDecimal("30"), item.calcularValorTotal());

        var mesmo = new PecaOrdemDeServico(novoId, peca, 1, BigDecimal.ONE);
        assertEquals(item, item);
        assertEquals(item, mesmo);
        assertNotEquals(item, new PecaOrdemDeServico(null, peca, 1, BigDecimal.ONE));
        assertNotEquals(item, "item");
        assertEquals(item.hashCode(), mesmo.hashCode());

        assertThrows(RegraNegocioException.class,
                () -> new PecaOrdemDeServico(id, null, 1, BigDecimal.ONE));
        assertThrows(RegraNegocioException.class,
                () -> new PecaOrdemDeServico(id, peca, null, BigDecimal.ONE));
        assertThrows(RegraNegocioException.class,
                () -> new PecaOrdemDeServico(id, peca, 0, BigDecimal.ONE));
        assertThrows(RegraNegocioException.class,
                () -> new PecaOrdemDeServico(id, peca, 1, null));
        assertThrows(RegraNegocioException.class,
                () -> new PecaOrdemDeServico(id, peca, 1, BigDecimal.ZERO));
        assertThrows(RegraNegocioException.class,
                () -> item.atualizarPecaOrdemDeServico(id, null, 1, BigDecimal.ONE));
        assertThrows(RegraNegocioException.class,
                () -> item.atualizarPecaOrdemDeServico(id, peca, -1, BigDecimal.ONE));
        assertThrows(RegraNegocioException.class,
                () -> item.atualizarPecaOrdemDeServico(id, peca, 1, BigDecimal.ZERO));
    }

    @Test
    void itemDeServicoDeveValidarExecutarCicloEComparar() {
        var id = UUID.randomUUID();
        var servico = new Servico("Troca", BigDecimal.TEN);
        var item = new ServicoOrdemDeServico(id, servico, new BigDecimal("9.00"));
        var inicio = LocalDateTime.of(2026, 7, 10, 9, 0);
        var fim = inicio.plusHours(1);

        assertEquals(StatusServicoEnum.AGUARDANDO, item.getStatus());
        item.iniciar(inicio);
        assertEquals(StatusServicoEnum.EM_EXECUCAO, item.getStatus());
        assertEquals(inicio, item.getDtInicio());
        assertThrows(RegraNegocioException.class, () -> item.iniciar(inicio));
        item.finalizar(fim);
        assertEquals(StatusServicoEnum.FINALIZADO, item.getStatus());
        assertEquals(fim, item.getDtFim());
        assertThrows(RegraNegocioException.class, () -> item.finalizar(fim));

        var reconstruido = new ServicoOrdemDeServico(id, servico, StatusServicoEnum.CANCELADO,
                inicio, fim, BigDecimal.TEN);
        assertEquals(StatusServicoEnum.CANCELADO, reconstruido.getStatus());
        assertEquals(item, item);
        assertEquals(item, reconstruido);
        assertNotEquals(item, new ServicoOrdemDeServico(null, servico, BigDecimal.ONE));
        assertNotEquals(item, "item");
        assertEquals(item.hashCode(), reconstruido.hashCode());

        var peloCatalogo = new ServicoOrdemDeServico(servico);
        assertEquals(servico.getPreco(), peloCatalogo.getValorCobrado());
        assertThrows(RegraNegocioException.class, () -> new ServicoOrdemDeServico((Servico) null));
        assertThrows(RegraNegocioException.class,
                () -> new ServicoOrdemDeServico(id, null, BigDecimal.ONE));
        assertThrows(RegraNegocioException.class,
                () -> new ServicoOrdemDeServico(id, servico, null));
        assertThrows(RegraNegocioException.class,
                () -> new ServicoOrdemDeServico(id, servico, BigDecimal.ZERO));
        assertThrows(RegraNegocioException.class,
                () -> peloCatalogo.finalizar(LocalDateTime.now()));
    }

    @Test
    void ordemDeveReconstruirAtualizarNegarOrcamentoEProtegerColecoes() {
        var id = UUID.randomUUID();
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        var operadorId = UUID.randomUUID();
        var peca = new PecaOrdemDeServico(UUID.randomUUID(),
                new Peca("Filtro", "Marca", BigDecimal.TEN), 2, BigDecimal.TEN);
        var servico = new ServicoOrdemDeServico(UUID.randomUUID(),
                new Servico("Troca", BigDecimal.TEN), BigDecimal.TEN);
        var ordem = new OrdemDeServico(id, clienteId, veiculoId, operadorId,
                StatusOrdemDeServicoEnum.EM_DIAGNOSTICO, null, null,
                new BigDecimal("30.00"), List.of(peca), List.of(servico));

        assertEquals(id, ordem.getId());
        assertEquals(new BigDecimal("30.00"), ordem.getValorTotalOs());
        assertEquals(1, ordem.getPecas().size());
        assertEquals(1, ordem.getServicos().size());
        assertThrows(UnsupportedOperationException.class, () -> ordem.getPecas().clear());
        assertThrows(UnsupportedOperationException.class, () -> ordem.getServicos().clear());

        var novoCliente = UUID.randomUUID();
        var novoVeiculo = UUID.randomUUID();
        var novoOperador = UUID.randomUUID();
        ordem.atualizarOrdemDeServico(novoCliente, novoVeiculo, novoOperador);
        assertEquals(novoCliente, ordem.getClienteId());
        assertEquals(novoVeiculo, ordem.getVeiculoId());
        assertEquals(novoOperador, ordem.getOperadorId());

        ordem.aguardarAprovacao();
        ordem.negarOrcamento();
        assertEquals(StatusOrdemDeServicoEnum.EM_DIAGNOSTICO, ordem.getStatus());

        var semItens = new OrdemDeServico(id, clienteId, veiculoId, operadorId,
                StatusOrdemDeServicoEnum.RECEBIDA, null, null, BigDecimal.ZERO, null, null);
        assertTrue(semItens.getPecas().isEmpty());
        assertTrue(semItens.getServicos().isEmpty());
        assertEquals(ordem, ordem);
        assertEquals(ordem, semItens);
        assertNotEquals(ordem, new OrdemDeServico(clienteId, veiculoId, operadorId));
        assertNotEquals(ordem, "ordem");
        assertEquals(ordem.hashCode(), semItens.hashCode());
    }

    @Test
    void ordemDeveValidarIdentificadoresItensEEstadoFinal() {
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        var operadorId = UUID.randomUUID();
        assertThrows(RegraNegocioException.class,
                () -> new OrdemDeServico(null, veiculoId, operadorId));
        assertThrows(RegraNegocioException.class,
                () -> new OrdemDeServico(clienteId, null, operadorId));
        assertThrows(RegraNegocioException.class,
                () -> new OrdemDeServico(clienteId, veiculoId, null));

        var ordem = new OrdemDeServico(clienteId, veiculoId, operadorId);
        assertThrows(RegraNegocioException.class,
                () -> ordem.atualizarOrdemDeServico(null, veiculoId, operadorId));
        assertThrows(RegraNegocioException.class, () -> ordem.adicionarPeca(null));
        assertThrows(RegraNegocioException.class, () -> ordem.adicionarServico(null));

        ordem.iniciarDiagnostico();
        ordem.aguardarAprovacao();
        ordem.aprovarOrcamento(LocalDateTime.now());
        ordem.finalizar(LocalDateTime.now());
        assertThrows(RegraNegocioException.class, () -> ordem.adicionarPeca(
                new PecaOrdemDeServico(UUID.randomUUID(),
                        new Peca("P", "M", BigDecimal.ONE), 1, BigDecimal.ONE)));
        assertThrows(RegraNegocioException.class, () -> ordem.adicionarServico(
                new ServicoOrdemDeServico(UUID.randomUUID(),
                        new Servico("S", BigDecimal.ONE), BigDecimal.ONE)));
        ordem.entregar();
        assertThrows(RegraNegocioException.class, ordem::iniciarDiagnostico);
    }
}
