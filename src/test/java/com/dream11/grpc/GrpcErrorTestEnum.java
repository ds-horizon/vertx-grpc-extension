package com.dream11.grpc;

import com.dream11.grpc.error.GrpcError;
import com.google.rpc.Code;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GrpcErrorTestEnum implements GrpcError {
  UNKNOWN_EXCEPTION("UNKNOWN_EXCEPTION", "Error:%s, message:%s", Code.UNKNOWN);

  final String errorCode;
  final String errorMessage;
  final Code grpcCode;
}
