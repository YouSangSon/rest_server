package yousang.rest.application.lotto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import yousang.rest.domain.lotto.LottoService
import yousang.rest.interfaces.lotto.LottoDto
import yousang.rest.shared.log.log

/**
 * 로또 번호 관련 비즈니스 로직을 처리하는 UseCase 클래스
 * 애플리케이션 계층의 진입점으로 도메인 서비스를 호출
 */
@Component
class LottoUseCase(private val lottoService: LottoService) {

    /**
     * 특정 범위의 로또 번호를 조회
     * @param firstDrwNo 시작 회차 번호
     * @param lastDrwNo 종료 회차 번호
     */
    suspend fun getLotto(firstDrwNo: Int, lastDrwNo: Int): List<LottoDto> {
        return lottoService.getLotto(firstDrwNo, lastDrwNo)
    }

    /**
     * 외부 API에서 최신 로또 번호를 가져와 데이터베이스에 저장합니다.
     */
    suspend fun putLotto() {
        lottoService.putLotto()
    }
}