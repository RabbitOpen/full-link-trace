package rabbit.flt.rpc.test;

import reactor.core.publisher.Mono;

public interface MonoUserService {

    Mono<String> getUserName();
}
