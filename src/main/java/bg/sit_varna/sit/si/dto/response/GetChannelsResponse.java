package bg.sit_varna.sit.si.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response containing list of available notification channels")
public record GetChannelsResponse(

        @Schema(description = "List of available channels")
        List<ChannelInfo> channels,

        @Schema(description = "Total number of channels",
                example = "4")
        int total
) {
    public GetChannelsResponse {
        if (channels != null) {
            channels = List.copyOf(channels);
        }
    }

    public static GetChannelsResponse of(List<ChannelInfo> channels) {
        int total = channels != null ? channels.size() : 0;
        return new GetChannelsResponse(channels, total);
    }

    @Schema(description = "Information about a notification channel")
    public record ChannelInfo(
            @Schema(description = "Channel name", example = "EMAIL")
            String name,

            @Schema(description = "Channel description",
                    example = "Send notifications via email")
            String description,

            @Schema(description = "Whether the channel is enabled",
                    example = "true")
            boolean enabled
    ) {}
}
