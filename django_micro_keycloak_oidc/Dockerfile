FROM python:2.7.10

COPY . /usr/src/kclexample
WORKDIR /usr/src/kclexample

RUN pip install -r requirements.txt --no-cache-dir --ignore-installed

EXPOSE 8000

ENTRYPOINT [ "./docker-entrypoint.sh" ]
