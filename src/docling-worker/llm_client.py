from __future__ import annotations

import asyncio
import os
from abc import ABC, abstractmethod

import httpx

_MAX_RETRIES = 3
_RETRY_STATUSES = {429, 500, 502, 503, 504}


def _is_retryable(exc: BaseException) -> bool:
    if isinstance(exc, (httpx.TimeoutException, httpx.ConnectError)):
        return True
    if isinstance(exc, httpx.HTTPStatusError):
        return exc.response.status_code in _RETRY_STATUSES
    # anthropic / openai SDK raise these on rate-limit / timeout / server error
    return type(exc).__name__ in {"RateLimitError", "APITimeoutError", "InternalServerError"}


async def _with_retry(label: str, coro_fn, *args, **kwargs) -> str:
    delay = 1.0
    for attempt in range(1, _MAX_RETRIES + 2):
        try:
            return await coro_fn(*args, **kwargs)
        except Exception as exc:
            if attempt > _MAX_RETRIES or not _is_retryable(exc):
                raise
            print(f"[{label}] attempt {attempt} failed ({type(exc).__name__}: {exc}); "
                  f"retrying in {delay:.0f}s")
            await asyncio.sleep(delay)
            delay *= 2


class LLMClient(ABC):
    @abstractmethod
    async def complete(self, system: str, user: str, max_tokens: int = 512) -> str: ...


class ClaudeClient(LLMClient):
    def __init__(self) -> None:
        import anthropic
        self._client = anthropic.AsyncAnthropic(api_key=os.environ["ANTHROPIC_API_KEY"])
        self._model = os.environ.get("LLM_MODEL", "claude-haiku-4-5")

    async def _call(self, system: str, user: str, max_tokens: int) -> str:
        msg = await self._client.messages.create(
            model=self._model,
            max_tokens=max_tokens,
            temperature=0,
            system=system,
            messages=[{"role": "user", "content": user}],
        )
        text = msg.content[0].text
        print(f"[llm:claude] stop_reason={msg.stop_reason} "
              f"input_tokens={msg.usage.input_tokens} output_tokens={msg.usage.output_tokens}")
        print(f"[llm:claude] response: {text[:500]}")
        return text

    async def complete(self, system: str, user: str, max_tokens: int = 512) -> str:
        print(f"[llm:claude] model={self._model} max_tokens={max_tokens} "
              f"system_len={len(system)} user_len={len(user)}")
        return await _with_retry("llm:claude", self._call, system, user, max_tokens)


class OpenAIClient(LLMClient):
    def __init__(self) -> None:
        import openai
        self._client = openai.AsyncOpenAI(api_key=os.environ["OPENAI_API_KEY"])
        self._model = os.environ.get("LLM_MODEL", "gpt-4o-mini")

    async def _call(self, system: str, user: str, max_tokens: int) -> str:
        resp = await self._client.chat.completions.create(
            model=self._model,
            max_tokens=max_tokens,
            temperature=0,
            messages=[
                {"role": "system", "content": system},
                {"role": "user", "content": user},
            ],
        )
        text = resp.choices[0].message.content
        usage = resp.usage
        print(f"[llm:openai] finish_reason={resp.choices[0].finish_reason} "
              f"prompt_tokens={usage.prompt_tokens} completion_tokens={usage.completion_tokens}")
        print(f"[llm:openai] response: {text[:500]}")
        return text

    async def complete(self, system: str, user: str, max_tokens: int = 512) -> str:
        print(f"[llm:openai] model={self._model} max_tokens={max_tokens} "
              f"system_len={len(system)} user_len={len(user)}")
        return await _with_retry("llm:openai", self._call, system, user, max_tokens)


class OllamaClient(LLMClient):
    """Calls a local Ollama server — no SDK dependency, just httpx."""

    def __init__(self) -> None:
        self._base_url = os.environ.get("OLLAMA_BASE_URL", "http://localhost:11434")
        self._model = os.environ.get("LLM_MODEL", "llama3.2")
        self._disable_thinking = os.environ.get("OLLAMA_DISABLE_THINKING", "true").lower() == "true"

    async def _call(self, system: str, user: str, max_tokens: int) -> str:
        url = f"{self._base_url}/api/chat"
        payload = {
            "model": self._model,
            "stream": False,
            "format": "json",
            "options": {"temperature": 0, "num_predict": max_tokens},
            "messages": [
                {"role": "system", "content": system},
                {"role": "user", "content": user},
            ],
        }
        if self._disable_thinking:
            payload["think"] = False
        print(f"[llm:ollama] format=json think={payload.get('think', 'default')}")
        print(f"[llm:ollama] user content (first 300 chars): {user[:300]}")
        async with httpx.AsyncClient(timeout=120.0) as client:
            resp = await client.post(url, json=payload)
            print(f"[llm:ollama] HTTP {resp.status_code}")
            resp.raise_for_status()
            body = resp.json()
            text = body.get("message", {}).get("content", "")
            thinking = body.get("message", {}).get("thinking", "")
            done_reason = body.get("done_reason", "unknown")
            eval_count = body.get("eval_count", "?")
            print(f"[llm:ollama] done_reason={done_reason} eval_count={eval_count}")
            if thinking:
                print(f"[llm:ollama] thinking (first 500 chars): {thinking[:500]}")
            print(f"[llm:ollama] response: {text[:500]}")
            return text or thinking

    async def complete(self, system: str, user: str, max_tokens: int = 512) -> str:
        print(f"[llm:ollama] POST {self._base_url}/api/chat model={self._model} "
              f"max_tokens={max_tokens} system_len={len(system)} user_len={len(user)}")
        return await _with_retry("llm:ollama", self._call, system, user, max_tokens)


def get_llm_client() -> LLMClient:
    """Factory — reads LLM_PROVIDER env var (claude|openai|ollama, default: claude)."""
    provider = os.environ.get("LLM_PROVIDER", "claude").lower()
    if provider == "openai":
        return OpenAIClient()
    if provider == "ollama":
        return OllamaClient()
    return ClaudeClient()
