package yousang.rest.application.lotto

import org.springframework.stereotype.Component
import yousang.rest.domain.lotto.AnnuityLottoService
import yousang.rest.interfaces.lotto.AnnuityLottoDto

/**
 * 연금복권 번호 관련 비즈니스 로직을 처리하는 UseCase 클래스
 * 애플리케이션 계층의 진입점으로 도메인 서비스를 호출
 */
@Component
class AnnuityLottoUseCase(private val annuityLottoService: AnnuityLottoService) {

    /**
     * 특정 범위의 연금복권 번호를 조회
     * @param firstDrwNo 시작 회차 번호
     * @param lastDrwNo 종료 회차 번호
     */
    suspend fun getAnnuityLotto(firstDrwNo: Int, lastDrwNo: Int): List<AnnuityLottoDto> {
        return annuityLottoService.getAnnuityLotto(firstDrwNo, lastDrwNo)
    }

    /**
     * 외부 API에서 최신 연금복권 번호를 가져와 데이터베이스에 저장합니다.
     */
    suspend fun putAnnuityLotto() {
        annuityLottoService.putAnnuityLotto()
    }
}