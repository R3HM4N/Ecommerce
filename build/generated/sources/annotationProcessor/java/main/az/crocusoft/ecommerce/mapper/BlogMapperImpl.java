package az.crocusoft.ecommerce.mapper;

import az.crocusoft.ecommerce.dto.BlogMainDto;
import az.crocusoft.ecommerce.model.Blog;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-12-07T00:56:37-0800",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.4.jar, environment: Java 18.0.2 (Amazon.com Inc.)"
)
@Component
public class BlogMapperImpl implements BlogMapper {

    @Override
    public Blog toEntity(BlogMainDto blogMainDto) {
        if ( blogMainDto == null ) {
            return null;
        }

        Blog.BlogBuilder blog = Blog.builder();

        blog.pid( blogMainDto.getPid() );
        blog.title( blogMainDto.getTitle() );
        blog.date( blogMainDto.getDate() );
        blog.content( blogMainDto.getContent() );

        return blog.build();
    }
}
