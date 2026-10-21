package com.concatstudio.onegym.routes
import com.concatstudio.onegym.model.News
import com.concatstudio.onegym.respository.NewsRepository
import com.concatstudio.onegym.security.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
fun Route.newsRouting(){ val repo:NewsRepository by inject(); val auth:AuthorizationService by inject(); route("/news") {
 get { if(!call.isSuperAdmin(auth)) call.respond(HttpStatusCode.Forbidden) else call.respond(repo.getNews()) }
 get("/{id}"){val id=call.parameters["id"]?.toLongOrNull()?:return@get call.respond(HttpStatusCode.BadRequest);val n=repo.getNewsById(id)?:return@get call.respond(HttpStatusCode.NotFound);val a=call.authenticatedUserId()?:return@get call.respond(HttpStatusCode.Unauthorized);if(!auth.isSuperAdmin(a)&&auth.gymRole(a,n.gymId)==null)call.respond(HttpStatusCode.Forbidden)else call.respond(n)}
 post{val n=call.receive<News>();val a=call.authenticatedUserId()?:return@post call.respond(HttpStatusCode.Unauthorized);if(!auth.canManageGym(a,n.gymId))call.respond(HttpStatusCode.Forbidden)else{repo.createNews(n);call.respond(HttpStatusCode.Created)}}
 put("/{id}"){val id=call.parameters["id"]?.toLongOrNull()?:return@put call.respond(HttpStatusCode.BadRequest);val old=repo.getNewsById(id)?:return@put call.respond(HttpStatusCode.NotFound);val n=call.receive<News>();val a=call.authenticatedUserId()?:return@put call.respond(HttpStatusCode.Unauthorized);if(old.gymId!=n.gymId||!auth.canManageGym(a,old.gymId))call.respond(HttpStatusCode.Forbidden)else{repo.updateNews(id,n);call.respond(HttpStatusCode.NoContent)}}
 delete("/{id}"){val id=call.parameters["id"]?.toLongOrNull()?:return@delete call.respond(HttpStatusCode.BadRequest);val n=repo.getNewsById(id)?:return@delete call.respond(HttpStatusCode.NotFound);val a=call.authenticatedUserId()?:return@delete call.respond(HttpStatusCode.Unauthorized);if(!auth.canManageGym(a,n.gymId))call.respond(HttpStatusCode.Forbidden)else{repo.deleteNewsById(id);call.respond(HttpStatusCode.NoContent)}}
} }
