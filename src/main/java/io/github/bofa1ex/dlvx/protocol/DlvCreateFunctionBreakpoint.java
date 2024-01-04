package io.github.bofa1ex.dlvx.protocol;

import com.goide.dlv.protocol.DlvApi;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jsonProtocol.OutMessage;
import org.jetbrains.jsonProtocol.Request;

import java.io.IOException;
import java.math.BigInteger;

public class DlvCreateFunctionBreakpoint extends OutMessage implements Request<DlvApi.Breakpoint> {
    public DlvCreateFunctionBreakpoint(@NotNull BigInteger addr) {
        try {
            JsonWriter w = this.getWriter();
            w.name("method").value(this.getMethodName());
            w.name("params").beginArray().beginObject();
            w.name("Breakpoint").beginObject()
                    .name("addr").value(addr)
                    .endObject();
            w.endObject().endArray();
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    @NotNull
    @Override
    public String getMethodName() {
        return "RPCServer.CreateBreakpoint";
    }

    @Override
    public void finalize(int id) {
        try {
            this.getWriter().name("id").value(id);
            this.getWriter().endObject();
            this.getWriter().close();
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }
}
