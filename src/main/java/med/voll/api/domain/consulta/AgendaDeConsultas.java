package med.voll.api.domain.consulta;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.validacoes.ValidadorAgendamentoDeConsulta;
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

        var consulta = consultaRepository.getReferenceById(dados.idConsulta());
        consulta.cancelar(dados.motivo());
    }
    /*
        As seguintes regras de negócio devem ser validadas pelo sistema:
        • O horário de funcionamento da clínica é de segunda a sábado, das 07:00 às
        19:00;
        • As consultas tem duração fixa de 1 hora;
        • As consultas devem ser agendadas com antecedência mínima de 30
        minutos;
        • Não permitir o agendamento de consultas com
        pacientes inativos no
        sistema;
        • Não permitir o agendamento de consultas com médicos inativos no sistema;
        • Não permitir o agendamento de mais de uma consulta no mesmo dia para
        um mesmo paciente;
        • Não permitir o agendamento de uma consulta com um médico que já possui
        outra consulta agendada na mesma data/hora;
        • A escolha do médico é opcional, sendo que nesse caso o sistema deve escolher aleatoriamente algum médico disponível na data/hora preenchida.
     */
}
