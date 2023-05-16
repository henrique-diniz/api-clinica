package med.voll.api.domain.consulta;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.validacoes.agendamento.ValidadorAgendamentoDeConsulta;
import med.voll.api.domain.consulta.validacoes.cancelamento.ValidadorCancelamentoDeConsulta;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendaDeConsultas {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private List<ValidadorAgendamentoDeConsulta> validadores;
    @Autowired
    private List<ValidadorCancelamentoDeConsulta> validadoresDeCancelamento;
    public  DadosDetalhamentoConsulta agendar (DadosAgendamentoConsulta dados) {
        if (!pacienteRepository.existsById(dados.idPaciente())) {
            throw new ValidacaoException("Paciente inexistente");
        }

        if (dados.idMedico() != null && !medicoRepository.existsById(dados.idMedico())) {
            throw new ValidacaoException("Medico inexistente");
        }

        //Single Reponsability Principle, Open Closed Principle, Dependency Inversion Principle
        validadores.forEach(v -> v.validar(dados));

        var paciente = pacienteRepository.findById(dados.idPaciente()).get();
        var medico = escolherMedico(dados);
        if(medico == null) {
            throw new ValidacaoException("Nao existe medico disponivel nesta data");
        }
        var consulta = new Consulta(null, medico, paciente, dados.data(), null);

        consultaRepository.save(consulta);
        return new DadosDetalhamentoConsulta(consulta);
    }

    private Medico escolherMedico(DadosAgendamentoConsulta dados) {
        if (dados.idMedico() != null) {
            return medicoRepository.getReferenceById(dados.idMedico());
        }

        if (dados.especialidade() == null) {
            throw new ValidacaoException("Especialidade vazia quando medico nao foi escolhido");
        }

        return medicoRepository.escolherMedicoAleatorioLivreNaData(dados.especialidade(), dados.data());
    }

    public void cancelar (DadosCancelamentoConsulta dados) {
        if (!consultaRepository.existsById(dados.idConsulta())) {
            throw new ValidacaoException("Consulta inexistente");
        }
        validadoresDeCancelamento.forEach(v -> v.validar(dados));

        var consulta = consultaRepository.getReferenceById(dados.idConsulta());
        consulta.cancelar(dados.motivo());
    }

}
